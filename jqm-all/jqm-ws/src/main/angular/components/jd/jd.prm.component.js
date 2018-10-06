'use strict';

import template from './jd.prm.template.html';


class jdPrmsDiagCtrl {
    // From bindings. Fields: jd, show. Callbacks: onClose.

    constructor() {
        this.data = {
            newKey: null,
            newValue: null
        };
    }

    addPrm() {
        this.jd.parameters.push({ key: this.data.newKey, value: this.data.newValue });
    };

    delPrm(prm) {
        this.jd.parameters.splice(this.jd.parameters.indexOf(prm), 1);
    };
};

export const jdPrmsDiagComponent = {
    controller: jdPrmsDiagCtrl,
    template: template,
    bindings: {
        jd: "=",
        show: "=",
        onclose: "<"
    }
};
