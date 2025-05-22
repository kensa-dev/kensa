import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';
import svgr from 'vite-plugin-svgr';
import cssInjectedByJsPlugin from 'vite-plugin-css-injected-by-js';
import path from 'path';

export default defineConfig({
    plugins: [
        react(),
        svgr(),
        cssInjectedByJsPlugin(),
        {
            name: 'dev-html-transform',
            apply: 'serve',
            transformIndexHtml(html) {
                return html.replace(
                    '</body>',
                    `<script type="module" src="/src/index.jsx"></script></body>`
                );
            },
        },

    ],
    css: {
        preprocessorOptions: {
            scss: {
                additionalData: `@import "bulma-custom";`
            },
        },
    },
    build: {
        rollupOptions: {
            input: '/src/index.jsx',
            output: {
                dir: 'build/js',
                entryFileNames: 'kensa.js',
                manualChunks: undefined
            },
        },
        outDir: 'build/js',
        emptyOutDir: true,
    },
    resolve: {
        alias: {
            '@': path.resolve(__dirname, 'src'),
            'bulma-custom': path.resolve(__dirname, 'bulma-custom.scss'),
        },
    },
    server: {
        port: 3000,
        open: '/index.html',
        strictPort: true,
    },
});