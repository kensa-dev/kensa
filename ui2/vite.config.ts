import {defineConfig, Plugin} from 'vite'
import react from '@vitejs/plugin-react'
import svgr from "vite-plugin-svgr"
import cssInjectedByJsPlugin from 'vite-plugin-css-injected-by-js';
import path from "path"

const host = process.env.TAURI_DEV_HOST;

const devDataPlugin = (): Plugin => ({
    name: 'serve-dev-data',
    configureServer(server) {
        server.middlewares.use((req, res, next) => {
            if (req.url?.endsWith('.json') || req.url?.includes('/results/')) {
                req.url = req.url.replace(/^\//, '/dev-data/');
            }
            next();
        });
    }
});

export default defineConfig({
    plugins: [
        react(),
        svgr(),
        cssInjectedByJsPlugin(),
        devDataPlugin(),
    ],
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src"),
        },
    },
    define: {
        'process.env': {},
    },
    clearScreen: false,
    server: {
        port: 5173,
        strictPort: true,
        host: host || false,
        fs: {
            allow: ['..', './dev-data']
        },
        hmr: host
            ? {
                protocol: 'ws',
                host,
                port: 1421,
            }
            : undefined,

        watch: {
            ignored: ['**/src-tauri/**'],
        },
    },
    envPrefix: ['VITE_', 'TAURI_ENV_*'],
    build: {
        outDir: 'build/js',
        emptyOutDir: true,
        cssCodeSplit: false,
        lib: {
            entry: path.resolve(__dirname, 'src/main.tsx'),
            name: 'Kensa2',
            fileName: () => `kensa2.js`,
            formats: ['iife'],
        },
        target:
            process.env.TAURI_ENV_PLATFORM == 'windows'
                ? 'chrome105'
                : 'safari13',
        minify: !process.env.TAURI_ENV_DEBUG ? 'esbuild' : false,
        sourcemap: !!process.env.TAURI_ENV_DEBUG,
    },
})