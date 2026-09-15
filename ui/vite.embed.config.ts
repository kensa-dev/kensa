import {defineConfig} from 'vite'
import path from 'path'

// The host-page script: a second, dependency-free iife beside kensa.js.
// Library mode takes one entry per build, so this is its own config, run
// after the main build (which empties build/js).
export default defineConfig({
    resolve: {
        alias: {
            "@": path.resolve(__dirname, "./src"),
        },
    },
    build: {
        outDir: 'build/js',
        emptyOutDir: false,
        lib: {
            entry: path.resolve(__dirname, 'src/embed/kensa-embed.ts'),
            name: 'KensaEmbed',
            fileName: () => `kensa-embed.js`,
            formats: ['iife'],
        },
        target:
            process.env.TAURI_ENV_PLATFORM == 'windows'
                ? 'chrome105'
                : 'safari13',
        minify: !process.env.TAURI_ENV_DEBUG ? 'esbuild' : false,
    },
})
