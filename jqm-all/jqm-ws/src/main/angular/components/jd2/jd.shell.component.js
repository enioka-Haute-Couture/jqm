'use strict';

import template from './jd.shell.template.html';

class JdShellCtrl
{
    constructor()
    {

    }
}


export const jdShellComponent = {
    controller: JdShellCtrl,
    template: template,
    bindings: {
        "jd": "=",
    }
};
