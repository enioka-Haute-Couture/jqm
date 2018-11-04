'use strict';

import template from './jd.cron.template.html';

class jdCronDiagController
{
    $onChanges()
    {
        this.selectedJd = this.ngModel;
        this.data['jd'] = this.selectedJd;

        this.selectedSchedule.length = 0;
        this.selectedSchedulePrm.length = 0;
    }

    constructor($interval, µQueueDto)
    {
        this.selectedSchedule = [];
        this.selectedSchedulePrm = [];
        this.queues = µQueueDto.query();

        this.$interval = $interval;

        this.data = {
            jd: this.selectedJd
        }

        // TODO: queue choice with empty value.
        /*this.queues.push({ id: null, name: 'none' });
        $.each(queues, function () {
            $scope.queues.push(this);
        });*/

        var $ctrl = this;
        this.gridOptionsCron = {
            data: '$ctrl.selectedJd.schedules',

            enableSelectAll: false,
            enableRowSelection: true,
            enableRowHeaderSelection: false,
            enableFullRowSelection: true,
            enableFooterTotalSelected: false,
            multiSelect: false,

            enableColumnMenus: false,
            enableCellEditOnFocus: true,
            virtualizationThreshold: 20,
            enableHorizontalScrollbar: 0,

            onRegisterApi: function (gridApi)
            {
                $ctrl.gridApiCron = gridApi;
                gridApi.selection.on.rowSelectionChanged(null, function (rows)
                {
                    $ctrl.selectedSchedule = gridApi.selection.getSelectedRows();
                });

                gridApi.cellNav.on.navigate(null, function (newRowCol, oldRowCol)
                {
                    if (newRowCol !== oldRowCol)
                    {
                        gridApi.selection.selectRow(newRowCol.row.entity);
                    }
                });
            },

            columnDefs: [
                {
                    field: 'cronExpression',
                    displayName: 'Cron expression',
                    width: '***',
                },
                {
                    field: 'queue',
                    displayName: 'Queue override',
                    cellTemplate: '<div class="ui-grid-cell-contents">{{ (row.entity["queue"] | getByProperty:"id":grid.appScope.$ctrl.queues).name }}</div>',
                    editableCellTemplate: 'ui-grid/dropdownEditor',
                    editDropdownValueLabel: 'name',
                    editDropdownOptionsArray: this.queues,
                    width: '*',
                },
            ]
        };

        this.gridOptionsCronPrm = {
            data: '$ctrl.selectedSchedule[0].parameters || []',

            enableSelectAll: false,
            enableRowSelection: true,
            enableRowHeaderSelection: false,
            enableFullRowSelection: true,
            enableFooterTotalSelected: false,
            multiSelect: false,

            enableColumnMenus: false,
            enableCellEditOnFocus: true,
            virtualizationThreshold: 20,
            enableHorizontalScrollbar: 0,

            onRegisterApi: function (gridApi)
            {
                $ctrl.gridApiCronPrm = gridApi;
                gridApi.selection.on.rowSelectionChanged(null, function (rows)
                {
                    $ctrl.selectedSchedulePrm = gridApi.selection.getSelectedRows();
                });

                gridApi.cellNav.on.navigate(null, function (newRowCol, oldRowCol)
                {
                    if (newRowCol !== oldRowCol)
                    {
                        gridApi.selection.selectRow(newRowCol.row.entity);
                    }
                });
            },

            columnDefs: [
                {
                    field: 'key',
                    displayName: 'Key',
                    width: '*',
                },
                {
                    field: 'value',
                    displayName: 'Value',
                    width: '*',
                },
            ]
        };
    }

    newcron()
    {
        var t = {
            id: null,
            cronExpression: "* * * * *",
            queue: null,
            parameters: [],
        };
        this.selectedJd.schedules.push(t);
        this.gridApiCron.selection.selectRow(t);
        var $ctrl = this;
        this.$interval(function ()
        {
            $ctrl.gridApiCron.cellNav.scrollToFocus(t, $ctrl.gridOptionsCron.columnDefs[0]);
        }, 0, 1);
    };

    removecron()
    {
        var q = null;
        for (var i = 0; i < this.selectedSchedule.length; i++)
        {
            q = this.selectedSchedule[i];
            this.selectedJd.schedules.splice(this.selectedJd.schedules.indexOf(q), 1);
        }
        this.selectedSchedule.length = 0;
    };

    newcronprm()
    {
        var t = {
            key: "key",
            value: "value",
        };
        this.selectedSchedule[0].parameters.push(t);
        this.gridApiCronPrm.selection.selectRow(t);
        var $ctrl = this;
        this.$interval(function ()
        {
            $ctrl.gridApiCronPrm.cellNav.scrollToFocus(t, $ctrl.gridOptionsCron.columnDefs[0]);
        }, 0, 1);
    };

    removecronprm()
    {
        var q = null;
        for (var i = 0; i < this.selectedSchedulePrm.length; i++)
        {
            q = this.selectedSchedulePrm[i];
            this.selectedSchedule[0].parameters.splice(this.selectedSchedule[0].parameters.indexOf(q), 1);
        }
        this.selectedSchedulePrm.length = 0;
    };
};
jdCronDiagController.$inject = ['$interval', 'µQueueDto',];

export const jdCronDiagComponent = {
    controller: jdCronDiagController,
    template: template,
    bindings: {
        'ngModel': '<',
        'show': '=',
    }
};
