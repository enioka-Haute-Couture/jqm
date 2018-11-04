'use strict';

import template from './jd.process.template.html';

class JdProcessCtrl
{
    constructor()
    {

    }
}


export const jdProcessComponent = {
    controller: JdProcessCtrl,
    template: template,
    bindings: {
        "jd": "=",
    }
};
