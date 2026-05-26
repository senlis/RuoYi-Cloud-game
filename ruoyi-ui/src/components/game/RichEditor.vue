<template>
  <div class="rich-editor">
    <div ref="editorContainer"></div>
  </div>
</template>

<script>
export default {
  name: "RichEditor",
  props: {
    value: { type: String, default: "" }
  },
  data() {
    return {
      editor: null,
      isUpdating: false
    }
  },
  watch: {
    value(val) {
      if (!this.isUpdating && this.editor && val !== undefined) {
        this.loadContent(val)
      }
    }
  },
  mounted() {
    this.$nextTick(() => this.initEditor())
  },
  beforeDestroy() {
    if (this.editor) {
      this.editor = null
    }
  },
  methods: {
    initEditor() {
      if (!window.Quill) {
        console.warn('Quill 未加载')
        return
      }
      this.editor = new window.Quill(this.$refs.editorContainer, {
        theme: 'snow',
        modules: {
          toolbar: [
            [{ header: [1, 2, 3, false] }],
            ['bold', 'italic', 'underline', 'strike'],
            [{ color: [] }, { background: [] }],
            [{ list: 'ordered' }, { list: 'bullet' }],
            [{ align: [] }],
            ['image'],
            ['clean']
          ]
        },
        placeholder: '输入邮件内容…'
      })

      if (this.value) {
        this.loadContent(this.value)
      }

      this.editor.on('text-change', () => {
        clearTimeout(this._syncTimer)
        this._syncTimer = setTimeout(() => {
          if (this.editor) this.syncContent()
        }, 100)
      })
    },

    syncContent() {
      const range = document.createRange()
      range.selectNodeContents(this.editor.root)
      const frag = range.cloneContents()
      const tmp = document.createElement('div')
      tmp.appendChild(frag)
      const html = tmp.innerHTML
      this.isUpdating = true
      this.$emit('input', html === '<br>' ? '' : html)
      this.$nextTick(() => { this.isUpdating = false })
    },

    loadContent(val) {
      if (!this.editor) return
      // 直接设置 innerHTML，和查看页面一致，确保所有格式正确显示
      this.editor.root.innerHTML = val || '<p><br></p>'
    }
  }
}
</script>

<style>
.rich-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
.rich-editor .ql-toolbar {
  border: none !important;
  border-bottom: 1px solid #ebeef5 !important;
  border-radius: 4px 4px 0 0;
  background: #fafafa;
}
.rich-editor .ql-container {
  min-height: 300px;
  max-height: 500px;
  border: none !important;
}
.rich-editor .ql-editor h1 { font-size: 2em !important; }
.rich-editor .ql-editor h2 { font-size: 1.5em !important; }
.rich-editor .ql-editor h3 { font-size: 1.17em !important; }
.rich-editor .ql-editor h4 { font-size: 1em !important; }
.rich-editor .ql-editor {
  min-height: 300px;
  max-height: 500px;
  overflow-y: auto;
  padding: 12px 16px !important;
  line-height: 1.7 !important;
  font-size: 14px !important;
}
.rich-editor .ql-editor p {
  margin-bottom: 8px !important;
}
</style>
