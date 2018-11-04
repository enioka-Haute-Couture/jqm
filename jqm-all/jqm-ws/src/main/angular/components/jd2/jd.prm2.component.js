'use strict';

import template from './jd.prm2.template.html';

class JdPrm2Ctrl
{
    constructor()
    {
        this.data = {
            newKey: null,
            newValue: null
        };
    }

    addPrm()
    {
        this.jd.parameters.push({ key: this.data.newKey, value: this.data.newValue });
    };

    delPrm(prm)
    {
        this.jd.parameters.splice(this.jd.parameters.indexOf(prm), 1);
    };
}


export const jdPrms2Component = {
    controller: JdPrm2Ctrl,
    template: template,
    bindings: {
        "jd": "=",
    }
};
