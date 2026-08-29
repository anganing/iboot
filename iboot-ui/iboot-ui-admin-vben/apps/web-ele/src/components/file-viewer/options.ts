import type { FileViewerOptions } from '@file-viewer/vue3';

import allRenderers from '@file-viewer/preset-all';

/**
 * 文件预览渲染器公共配置：图片/Office/PDF/视频/CAD 图纸等全格式
 * 各处使用 FileViewer 预览文件时统一复用
 */
export const fileViewerOptions: FileViewerOptions = {
  preset: allRenderers,
  rendererMode: 'replace',
  theme: 'light',
  styleIsolation: 'shadow',
  cad: {
    wasmPath: '/wasm/cad',
    workerUrl: '/wasm/cad/dwg-worker.js',
    dwfWasmUrl: '/wasm/cad/dwfv-render.wasm',
  },
  drawing: {
    viewerScriptUrl: '/vendor/drawio/viewer-static.min.js',
  },
  toolbar: {
    position: 'bottom-right',
    download: true,
    print: true,
    exportHtml: false,
  },
};
