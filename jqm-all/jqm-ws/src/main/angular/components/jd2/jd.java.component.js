'use strict';

import template from './jd.java.template.html';

class JdJavaCtrl
{
    constructor()
    {

    }
}


export const jdJavaComponent = {
    controller: JdJavaCtrl,
    template: template,
    bindings: {
        "jd": "="
    }
};
