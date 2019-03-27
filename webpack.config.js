const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const path = require('path');
const ROOT = path.resolve(__dirname, 'src/ui');
const ENTRY = path.resolve(ROOT, 'index.js');
const INDEX = path.resolve(ROOT, 'public/index.html');
const OUTPUT = path.resolve(__dirname, 'build/resources/main');

// const Visualizer = require('webpack-visualizer-plugin');

module.exports = {
    entry: ENTRY,
    output: {
        path: OUTPUT,
        filename: 'kensa.js'
    },
    // stats: 'errors-only',
    module: {
        rules: [
            {
                test: /\.scss$/,
                exclude: /node_modules/,
                use: [
                    // fallback to style-loader in development
                    process.env.NODE_ENV !== 'production' ? 'style-loader' : MiniCssExtractPlugin.loader,
                    'css-loader',
                    'sass-loader'
                ]
            },
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: {
                    loader: "babel-loader"
                }
            }
        ]
    },
    plugins: [
        new webpack.DefinePlugin({
            'process.env': {
                'NODE_ENV': JSON.stringify('production')
            }
        }),
        new MiniCssExtractPlugin({
            filename: "[name].css",
            chunkFilename: "[id].css"
        }),
        new HtmlWebpackPlugin({
            template: INDEX,
            filename: "./index.html",
            inlineSource: '.(js|css)$'
        }),
        new HtmlWebpackPlugin({
            template: INDEX,
            filename: "./pebble-index.html",
            inlineSource: '.(js|css)$',
            pebbleScript: "{% if scripts is not empty %}\n" +
                    "    {% for script in scripts %}\n" +
                    "        <script id=\"{{ script.id }}\" type=\"application/json\">{{ script.content }}</script>\n" +
                    "    {% endfor %}\n" +
                    "{% endif %}\n" +
                    "{% if indices is not empty %}\n" +
                    "<script id=\"indices\" type=\"application/json\">" +
                    "{\"mode\": \"{{ mode }}\", \"indices\":[{% for index in indices %}{{ index.content }}{{ loop.last ? \"\" : \",\" }}{% endfor %}]}" +
                    "</script>\n" +
                    "{% endif %}\n"
        })
        // ,
        // new Visualizer()
    ]
};