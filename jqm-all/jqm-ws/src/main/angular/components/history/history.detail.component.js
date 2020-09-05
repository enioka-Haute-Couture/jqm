'use strict';

import template from './history.detail.template.html';

class HistoryDetailController
{
    $doCheck()
    {
        if(this.ji !== this.previousJi || this.show !== this.previousShow)
        {
            this.dels.length = 0;
            this.getdel();
            this.previousJi = this.ji;
            this.previousShow = this.show;
        }
    }

    // From bindings : ji, show.
    constructor($http)
    {
        this.$http = $http;
        this.dialogId = ("dlg-" + Math.random()).replace('.', '');
        this.dels = [];
        this.previousJi = null;
        this.previousShow = null;

        this.getdel = this.getdel.bind(this);
        this.getdelOk = this.getdelOk.bind(this);

        this.getdel();
    }

    getdel()
    {
        if (this.show && this.ji)
        {
            this.$http.get("ws/client/ji/" + this.ji.id + "/files").then(this.getdelOk);
        }        
    };

    getdelOk(response)
    {
        this.dels = response.data;
    };
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

