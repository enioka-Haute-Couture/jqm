const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function (app)
{
    let apiTargetPort = 59977
    if (process.env.JQM_CONSOLE_PORT)
    {
        apiTargetPort = process.env.JQM_CONSOLE_PORT
    }
    console.log(apiTargetPort);

    app.use(
        '/ws',
        createProxyMiddleware({
            target: `http://localhost:${apiTargetPort}/ws`,
            changeOrigin: true,
        })
    );
};
