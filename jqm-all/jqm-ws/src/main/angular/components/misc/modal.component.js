'use strict';

import template from './modal.template.html';
import $ from 'jquery'; // needed by bootstrap js.
import 'bootstrap/js/dist/modal';

class ModalController {
    constructor() {
        this.dialogId = ("dlg-" + Math.random()).replace('.', '');

        // Ng way of registering an after DOM init hook...
        angular.element(this.afterDomReady.bind(this));
    }

    afterDomReady() {
        this.dialog = $("#" + this.dialogId);

        var $ctrl = this;
        this.dialog.on('hidden.bs.modal', function (e) {
            if ($ctrl.onclose) {
                $ctrl.onclose();
            }

            // Always reset parent 'show' attribute, as dialog may not have been closed through close button.
            $ctrl.show = false;
        });

        this.toggleModal();
    }

    $onDestroy() {
        // Remove close dialog handler - avoids leaks.
        this.dialog.off('hidden.bs.modal');
    }

    // Called during digest cycle. We use it to determine if we should show the dialog after a 'show' change.
    $doCheck() {
        if (this.previousShow !== this.show) {
            this.toggleModal();
        }
        this.previousShow = this.show;
    }

    toggleModal() {
        if (!this.dialog) { return; }
        if (this.show) {
            this.dialog.modal('show');
        }
        else {
            this.dialog.modal('hide');
        }
    }

    hide() {
        // Showing dialog is actually bound to 'show'.
        this.show = false;
        // event handlers are called later since we listen to hidden.bs.modal
    };
}

export const modalComponent = {
    controller: ModalController,
    template: template,
    transclude: {
        'footer': '?modalFooter'
    },
    bindings: {
        'title': '@',
        'show': '=',
        'onclose': '<',
    }
};