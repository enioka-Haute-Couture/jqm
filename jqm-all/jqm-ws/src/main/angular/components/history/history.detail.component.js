'use strict';

import template from './history.detail.template.html';

//  function ($scope, $http, $uibModal, ji) {
class HistoryDetailController {

    // From bindings : ji, show.
    constructor($http) {

        this.$http = $http;
        this.dialogId = ("dlg-" + Math.random()).replace('.', '');
        this.dels = [];

        this.getdel();
    }

    getdel() {
        if (this.show && this.ji) {
            this.$http.get("ws/client/ji/" + this.ji.id + "/files").then(this.getdelOk.bind(this));
        }
    };

    getdelOk(response) {
        this.dels = response.data;
    };

    /*showlog(url) {
        $uibModal.open({
            templateUrl: './template/file_reader.html',
            controller: 'fileReader',
            size: 'lg',

            resolve: {
                url: function () {
                    return url;
                },
            },
        });
    };*/
};
HistoryDetailController.$inject = ['$http',];


export const historyDetailComponent = {
    controller: HistoryDetailController,
    template: template,
    bindings: {
        'ji': '<',
        'show': '=',
    }
};

