'use strict';

import template from './jd.cl.template.html';

class jdClDiagController {

    constructor() {
        this.data = {
            specificIsolationContext: $scope.selectedJd.specificIsolationContext,
            hiddenJavaClasses: $scope.selectedJd.hiddenJavaClasses,
            childFirstClassLoader: $scope.selectedJd.childFirstClassLoader,
        };
    }

    ok() {
        this.ngModel.childFirstClassLoader = this.data.childFirstClassLoader;
        this.ngModel.hiddenJavaClasses = this.data.hiddenJavaClasses;
        this.ngModel.specificIsolationContext = this.data.specificIsolationContext;

        this.show = false;
    };
};

export const jdClDiagComponent = {
    controller: jdClDiagController,
    template: template,
    bindings: {
        'ngModel': '=',
        'show': '='
    }
};
