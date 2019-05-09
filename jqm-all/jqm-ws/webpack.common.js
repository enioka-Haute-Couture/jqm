const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');

module.exports = {
    entry: './src/main/angular/app.js',
    plugins: [
        new CleanWebpackPlugin(),
        new HtmlWebpackPlugin({
            title: 'JQM administration',
            template: 'src/main/angular/index.wptemplate.html',
            inject: false,
            favicon: 'src/main/angular/css/favicon.png',
            filename: '../index.html'
        })],
    output: {
        filename: '[name].bundle.[contenthash].js',
        path: path.resolve(__dirname, 'src/main/webapp/dist'),
        publicPath: '/dist/'
    },
    module: {
        rules: [{
            test: /\.css$/,
            use: ['style-loader', 'css-loader']
        }, {
            test: /\.(png|svg|jpg|gif)$/,
            use: ['file-loader']
        }, {
            test: /\.(woff|woff2|eot|ttf|otf)$/,
            use: ['file-loader']
        }, {
            test: /components.*\.html$/,
            use: ['raw-loader']
        },
        {
            test: /app\.css$/,
            sideEffects: true,
        },]
    },
    optimization: {
        runtimeChunk: 'single',
        splitChunks: {
            cacheGroups: {
                vendor: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendors',
                    chunks: 'all'
                }
            }
        }
    }
};
