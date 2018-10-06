'use strict';

import $ from 'jquery';
import 'select2/dist/js/select2.full';
import 'select2/dist/css/select2.min.css';


class Select2Controller {
    constructor() {
        this.parentId = ("parent-" + Math.random()).replace('.', '');
        this.selectId = ("select2-" + Math.random()).replace('.', '');
        angular.element(this.afterDomReady.bind(this));
    }

    $onInit() {
        this.display = this.displayKey || 'name';
    }

    afterDomReady() {
        $("#" + this.selectId).select2({
            allowClear: this.clearable,
            multiple: false,
            placeholder: this.placeholder,
            width: '100%',
            dropdownParent: $("#" + this.parentId),
            //containerCssClass: 'form-control',
            //dropdownCssClass: 'form-control',
        });
    }
}

export const select2Component = {
    controller: Select2Controller,
    template: '<div id="{{$ctrl.parentId}}"><select id="{{$ctrl.selectId}}" ng-model="$ctrl.ngModel" ng-options="q.id as q[$ctrl.display] for q in $ctrl.data"></select></div>',
    bindings: {
        'data': '<',
        'ngModel': '=',
        'displayKey': '@',
        'placeholder': '@',
        'clearable': '@',
    }
};
