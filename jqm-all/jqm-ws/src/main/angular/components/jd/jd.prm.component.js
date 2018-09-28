'use strict';

import template from './jd.prm.template.html';
import $ from 'jquery'; // needed by bootstrap js.
import 'bootstrap/js/dist/modal';



class jdPrmsDiagCtrl
{
    // From bindings. Fields: jd, show. Callbacks: onClose.

    constructor()
    {
        this.data = {
            newKey: null,
            newValue: null
        };

        this.dialogId = ("dlg-" + Math.random()).replace('.', '');

        // Ng way of registering an after DOM init hook...
        angular.element(this.afterDomReady.bind(this));
    }

    afterDomReady()
    {
        this.dialog = $("#" + this.dialogId);
        var $ctrl = this;
        this.dialog.on('hidden.bs.modal', function (e)
        {
            $ctrl.onclose();
        });
        this.displayModal();
    }

    // Cleanup on destruction to avoid memory leaks.
    $onDestroy()
    {
        this.dialog.off('hidden.bs.modal');
    }

    // Called when the bindings change.
    $onChanges(bindings)
    {
        this.displayModal();
    }

    displayModal()
    {
        if (!this.dialog) { return; }
        if (this.show)
        {
            this.dialog.modal('show');
        }
        else
        {
            this.dialog.modal('hide');
        }
    }

    addPrm()
    {
        this.jd.parameters.push({ key: this.data.newKey, value: this.data.newValue });
    };

    delPrm(prm)
    {
        this.jd.parameters.splice(this.jd.parameters.indexOf(prm), 1);
    };

    done()
    {
        this.dialog.modal('hide');
    };
};

export const jdPrmsDiagComponent = {
    controller: jdPrmsDiagCtrl,
    template: template,
    bindings: {
        jd: "=",
        show: "<",
        onclose: "&"
    }
};
