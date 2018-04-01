var webpack = require('webpack');
var path = require('path');

var BUILD_DIR = path.resolve(__dirname, 'src/main/webapp/resources/javascript/public');
var APP_DIR = path.resolve(__dirname, 'src/main/webapp/resources/javascript/app');

var config = {
    context: APP_DIR,
    entry: {
        index:'./index.jsx',
        import:'./import.jsx'
    },
    output: {
        path: BUILD_DIR,
        filename: '[name].bundle.js'
    },
    module: {
        loaders: [
            {
                test: /\.jsx?/,
                include: APP_DIR,
                loader: 'babel-loader',
                query:{
                    plugins:[
                        "transform-class-properties","transform-decorators-legacy"
                    ]
                }
            }
        ]
    }
};

module.exports = config;