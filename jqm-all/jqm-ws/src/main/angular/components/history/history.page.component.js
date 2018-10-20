'use strict';

import template from './history.page.template.html';
import $ from 'jquery'; // needed by bootstrap js.
import 'bootstrap/js/dist/button';
import 'bootstrap/js/dist/popover';

class HistoryPageCtrl
{
    constructor(µQueueDto, $http)
    {
        this.$http = $http;

        this.data = null;
        this.selected = [];
        this.queues = µQueueDto.query();
        this.target = "hist";
        this.filterDate = false;
        this.showDetails = false;

        // Default range is three hours from now
        this.now = (new Date()).getTime();
        this.daterangemin = this.now - 36000000;
        this.daterangemax = this.now;
        this.datemin = this.now - 3600000 * 3;
        this.datemax = this.now;
        this.step = 600000;
        this.scale = 86400000;

        // Query
        this.query = {};

        this.pagingOptions = {
            pageSize: 15,
            currentPage: 1
        };

        this.sortInfo = [
            {
                col: 'ID',
                order: 'DESCENDING'
            }
        ];

        // DT
        var ctrl = this;
        this.gridOptions = {
            data: '$ctrl.data',
            enableSelectAll: false,
            enableRowSelection: true,
            enableRowHeaderSelection: false,
            enableFullRowSelection: true,
            enableFooterTotalSelected: false,
            multiSelect: false,
            enableSelectionBatchEvent: false,
            noUnselect: true,

            onRegisterApi: function (gridApi)
            {
                ctrl.gridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged(null, function (rows)
                {
                    ctrl.selected = gridApi.selection.getSelectedRows();
                });

                gridApi.core.on.sortChanged(null, function (grid, sortColumns)
                {
                    ctrl.sortInfo.length = 0;
                    $.each(sortColumns, function ()
                    {
                        ctrl.sortInfo.push({ col: ctrl.colDef.sortField, order: ctrl.sort.direction === "desc" ? "DESCENDING" : "ASCENDING" });
                    });

                    ctrl.getDataAsync();
                });

                gridApi.pagination.on.paginationChanged(null, function (newPage, pageSize)
                {
                    ctrl.pagingOptions.currentPage = newPage;
                    ctrl.pagingOptions.pageSize = pageSize;
                    ctrl.getDataAsync();
                });
            },

            enableColumnMenus: false,
            enableCellEditOnFocus: false,
            virtualizationThreshold: 20,
            enableHorizontalScrollbar: 0,
            showFooter: true,

            paginationPageSizes: [10, 15, 20, 30, 40, 50, 100],
            paginationPageSize: 20,
            useExternalPagination: true,
            useExternalSorting: true,

            appScopeProvider: {
                onDblClick: function (row)
                {
                    ctrl.showDetails = true;
                }
            },
            rowTemplate: '<div ng-dblclick="grid.appScope.onDblClick(row)" ng-repeat="(colRenderIndex, col) in colContainer.renderedColumns track by col.uid" ui-grid-one-bind-id-grid="rowRenderIndex + \'-\' + col.uid + \'-cell\'" class="ui-grid-cell" ng-class="{ \'ui-grid-row-header-cell\': col.isRowHeader }" role="{{col.isRowHeader ? \'rowheader\' : \'gridcell\'}}" ui-grid-cell> </div>',

            columnDefs: [{
                field: 'id',
                displayName: 'ID',
                width: '*',
                sortField: 'ID',
                sort: { direction: "desc" },
            }, {
                field: 'applicationName',
                displayName: 'Application',
                width: '***',
                sortField: 'APPLICATIONNAME',
            }, {
                field: 'queueName',
                displayName: 'Queue',
                width: '**',
                sortField: 'QUEUENAME',
            }, {
                field: 'state',
                displayName: 'Status',
                width: '*',
                sortField: 'STATUS',
                cellTemplate: '<div ng-class="{\'bg-success\': row.entity[col.field] == \'ENDED\', \
                                                   \'bg-info\': row.entity[col.field] == \'RUNNING\', \
                                                   \'bg-danger\': row.entity[col.field] == \'CRASHED\' || row.entity[col.field] == \'KILLED\' || row.entity[col.field] == \'CANCELLED\',\
                                                   \'bg-warning\': row.entity[col.field] == \'SUBMITTED\' }"> \
                                                   <div class="ui-grid-cell-contents">{{row.entity[col.field]}}</div></div>',
            }, {
                field: 'enqueueDate',
                displayName: 'Enqueued',
                cellFilter: 'date : "dd/MM HH:mm:ss"',
                width: '**',
                sortField: 'DATEENQUEUE',
            }, {
                field: 'beganRunningDate',
                displayName: 'Began',
                cellFilter: 'date : "dd/MM HH:mm:ss"',
                width: '**',
                sortField: 'DATEEXECUTION',
            }, {
                field: 'endDate',
                displayName: 'Ended',
                cellFilter: 'date : "dd/MM HH:mm:ss"',
                width: '**',
                sortField: 'DATEEND',
            }, {
                field: 'user',
                displayName: 'User',
                width: '**',
                sortField: 'USERNAME',
            }, {
                field: 'parent',
                displayName: 'Parent',
                width: '*',
                sortField: 'PARENTID',
            }, {
                field: 'progress',
                displayName: 'Progress',
                width: '*',
                enableSorting: false,
            }, {
                field: 'sessionID',
                displayName: 'Session ID',
                width: '*',
                enableSorting: false,
            },]
        };

        // Go
        this.getDataAsync();
    }

    $onInit()
    {
        $('[data-toggle="popover"]').popover();
    }

    getDataOk(response)
    {
        this.data = response.data.instances;
        this.gridOptions.totalItems = response.data.resultSize;

        // Reset the time slider to avoid time drift.
        if (this.now < (new Date()).getTime() - 30000)
        {
            this.now = (new Date()).getTime();
            if (this.datemax === this.daterangemax)
                this.datemax = this.now;
            this.daterangemax = this.now;
            this.scaleX(this.scale);
        }
    };

    getDataAsync()
    {
        // Paging options
        this.query.firstRow = (this.pagingOptions.currentPage - 1) * this.pagingOptions.pageSize;
        this.query.pageSize = this.pagingOptions.pageSize;

        // History or queues?
        if (this.target === "hist")
        {
            this.query.queryLiveInstances = false;
            this.query.queryHistoryInstances = true;
        }
        else
        {
            this.query.queryLiveInstances = true;
            this.query.queryHistoryInstances = false;
        }

        // KO only?
        if (this.target === "hist" && this.ko)
        {
            this.query.statuses = ['CRASHED', 'KILLED',];
        }
        else
        {
            this.query.statuses = [];
        }

        // Running only?
        if (this.target === "queues" && this.running)
        {
            this.query.statuses = ['RUNNING',];
        }

        // Sort options
        this.query.sortby = this.sortInfo;

        // Time filters
        delete this.query.enqueuedBefore;
        delete this.query.enqueuedAfter;
        if (this.filterDate)
        {
            if (this.datemax !== this.daterangemax)
            {
                this.query.enqueuedBefore = new Date(this.datemax);
            }
            else
            {
                delete this.query.enqueuedBefore;
            }
            this.query.enqueuedAfter = new Date(this.datemin);
        }

        // Lists
        if (this.query.applicationName && this.query.applicationName.indexOf(',') > -1)
        {
            this.query.applicationName = this.query.applicationName.split(',');
        }

        this.onLaunched = this.onLaunched.bind(this);

        // Go
        this.$http.post("ws/client/ji/query", this.query).then(this.getDataOk.bind(this));
    };

    /*scaleX(s)
    {
        this.daterangemin = this.now - s;
        if (this.datemax < this.daterangemin)
        {
            this.datemax = this.daterangemax;
        }
        if (this.datemin < this.daterangemin)
        {
            this.datemin = this.daterangemin;
        }
    }

    this.$watch('scale', function (newVal, oldVal) {
        scale(newVal);
    });
    this.$watch('[target, ko, running, filterOptions, filterDate]', function (newVal, oldVal) {
        if (newVal !== oldVal) {
            this.selected.length = 0;
            this.pagingOptions.currentPage = 1;
            this.getDataAsync();
        }
    }, true);
    this.$watch('datemin', function (newVal, oldVal) {
        // Different watch - this one is debounced manually, as the slider does not support ng-model-options
        setTimeout(function () {
            if (newVal === this.datemin && this.filterDate) {
                // Has not changed in the past xx milliseconds => do the query.
                this.selected.length = 0;
                this.pagingOptions.currentPage = 1;
                this.getDataAsync();
            }
        }, 100);
    }, true);
    this.$watch('datemax', function (newVal, oldVal) {
        // Different watch - this one is debounced manually, as the slider does not support ng-model-options
        setTimeout(function () {
            if (newVal === this.datemax && this.filterDate) {
                // Has not changed in the past xx milliseconds => do the query.
                this.selected.length = 0;
                this.pagingOptions.currentPage = 1;
                this.getDataAsync();
            }
        }, 100);
    }, true);*/

    showDetail()
    {
        $uibModal.open({
            templateUrl: './template/history_detail.html',
            controller: 'historyDetail',
            size: 'lg',

            resolve: {
                ji: function ()
                {
                    return this.selected[0];
                }
            },
        });
    };

    // Buttons
    relaunch()
    {
        var ji = this.selected[0];
        this.target = 'queues';
        this.$http.post("ws/client/ji/" + ji.id).then(this.getDataAsync.bind(this));
    };

    onLaunched()
    {
        this.target = 'queues';
        this.getDataAsync();
    }

    kill()
    {
        var ji = this.selected[0];
        this.$http.post("ws/client/ji/killed/" + ji.id).then(this.getDataAsync.bind(this));
        this.selected.length = 0;
    };

    changeQueue(newqueueid)
    {
        var ji = this.selected[0];
        this.$http.post("ws/client/q/" + newqueueid + "/" + ji.id).then(this.getDataAsync.bind(this));
    };

    pause()
    {
        var ji = this.selected[0];
        this.$http.post("ws/client/ji/paused/" + ji.id).then(this.getDataAsync.bind(this));
        this.selected.length = 0;
    };

    resume()
    {
        var ji = this.selected[0];
        this.$http.delete("ws/client/ji/paused/" + ji.id).then(this.getDataAsync.bind(this));
        this.selected.length = 0;
    };
};
HistoryPageCtrl.$inject = ['µQueueDto', '$http'];


export const historyPageComponent = {
    controller: HistoryPageCtrl,
    template: template,
    bindings: {}
};
