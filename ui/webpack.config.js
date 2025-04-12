const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const path = require('path');
const ENTRY = path.resolve( 'index.js');
const INDEX = path.resolve( 'public/index.html');
const FAVICON = path.resolve( 'public/favicon.ico');
const OUTPUT = path.resolve(__dirname, 'build/js');
const CopyWebpackPlugin = require('copy-webpack-plugin');

// const Visualizer = require('webpack-visualizer-plugin');

module.exports = {
    entry: ENTRY,
    output: {
        path: OUTPUT,
        filename: 'kensa.js'
    },
    devServer: {
        static: {
             directory: path.join(__dirname, 'src/ui/public')
        }
    },
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
                exclude: /node_modules|\.test\.js$/,
                use: {
                    loader: "babel-loader"
                }
            }
        ]
    },
    plugins: [
        new webpack.DefinePlugin({}),
        new MiniCssExtractPlugin({
            filename: "[name].css",
            chunkFilename: "[id].css"
        }),
        new CopyWebpackPlugin({
            patterns: [
                { from: FAVICON, to: OUTPUT } // Adjust paths as needed
            ]
        }),
        new HtmlWebpackPlugin({
            template: INDEX,
            inject: false,
            filename: "./pebble-index.html",
            inlineSource: '.(js|css)$',
            pebbleScript: "{% if scripts is not empty %}\n" +
                    "    {% for script in scripts %}\n" +
                    "        <script id=\"{{ script.id }}\" type=\"application/json\">{{ script.content }}</script>\n" +
                    "    {% endfor %}\n" +
                    "{% endif %}\n" +
                    "{% if indices is not empty %}\n" +
                    "<script id=\"indices\" type=\"application/json\">" +
                    "{\"indices\":[{% for index in indices %}{{ index.content }}{{ loop.last ? \"\" : \",\" }}{% endfor %}]}" +
                    "</script>\n" +
                    "{% endif %}\n"
        })
        // ,
        // new Visualizer()
    ]
};