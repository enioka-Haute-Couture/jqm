'use strict';

import template from './jndi.list.template.html';
import { resourceTemplates } from './jndi.constants';


class JndiListCtrl
{
    constructor(µJndiDto, jqmCellTemplateBoolean, jqmCellEditorTemplateBoolean, $interval)
    {
        this.µJndiDto = µJndiDto;
        this.$interval = $interval;

        var ctx = this;
        this.resources = null;
        this.selected = [];
        this.selected2 = [];
        this.second = null;

        this.gridOptions = {
            data: '$ctrl.resources',

            // TODO: re-enable grouping when stable in uigrid library.
            enableSelectAll: false,
            enableRowSelection: true,
            enableRowHeaderSelection: false,
            enableFullRowSelection: true,
            enableFooterTotalSelected: false,
            multiSelect: false,
            enableSelectionBatchEvent: false,
            noUnselect: true,

            enableColumnMenus: false,
            enableCellEditOnFocus: true,
            virtualizationThreshold: 20,
            enableHorizontalScrollbar: 0,

            onRegisterApi: function (gridApi)
            {
                ctx.gridApi = gridApi;
                gridApi.selection.on.rowSelectionChanged(null, function (rows)
                {
                    ctx.selected = gridApi.selection.getSelectedRows();
                });

                gridApi.cellNav.on.navigate(null, function (newRowCol, oldRowCol)
                {
                    if (newRowCol !== oldRowCol)
                    {
                        gridApi.selection.selectRow(newRowCol.row.entity); // $scope.gridOptions.data[0]); // $scope.resources[0]
                    }
                });
            },

            columnDefs: [{
                field: 'name',
                displayName: 'JNDI alias',
                width: 150,
            }, {
                field: 'type',
                displayName: 'Type (class qualified name)',
            }, {
                field: 'factory',
                displayName: 'Factory',
            }, {
                field: 'description',
                displayName: 'Description',
            }, {
                field: 'singleton',
                displayName: 'S',
                width: 50,
                cellTemplate: jqmCellTemplateBoolean,
                editableCellTemplate: jqmCellEditorTemplateBoolean,
            },]
        };

        this.gridOptions2 = {
            data: '$ctrl.selected[0].parameters',

            enableSelectAll: false,
            enableRowSelection: true,
            enableRowHeaderSelection: true,
            enableFullRowSelection: false,
            enableFooterTotalSelected: false,
            multiSelect: true,

            enableColumnMenus: false,
            enableCellEditOnFocus: true,
            virtualizationThreshold: 20,
            enableHorizontalScrollbar: 0,

            onRegisterApi: function (gridApi)
            {
                ctx.gridApi2 = gridApi;
                gridApi.selection.on.rowSelectionChanged(null, function (rows)
                {
                    ctx.selected2 = gridApi.selection.getSelectedRows();
                });
            },

            columnDefs: [{
                field: 'key',
                displayName: 'Resource parameter',
                width: 160,
            }, {
                field: 'value',
                displayName: 'Value',
            },]
        };

        this.helpcontent = {
            title: "Resources are Java objects which can be requested by jobs through a JNDI lookup.",
            paragraphs: ["On this page, one may create or change resources. Creating a new resource does not require any reboot. Altering an already used <strong>singleton</strong> resource does require rebooting the nodes using it, as singleton resources are cached. Altering a non-singleton resource does not require any reboot.",
                "Please note that for a non-singleton resource, its class and factory class must be accessible to the payload: either in JQM_ROOT/ext or inside the payload's own dependencies. For a singleton resource, the classes must be inside ext (as they must be available to the engine itself, not only to the payload)."
            ],
            columns: {
                "JNDI alias": "The name that will be used by payloads during resource lookup.",
                "Type": "The fully qualified name of the resource class. E.g. java.io.File.File",
                "Factory": "The fully qualified name of the resource factory class. E.g. com.enioka.jqm.providers.FileFactory",
                "Description": "Free text to remember what the resource is for.",
                "S": "Tick for singleton resource. Differing from standard JNDI resource, a lookup on a singleton resource will always return the same instance (standard JNDI is: a new instance is created on each lookup). This is most helpful for connection pools, for which it is stupid to create a new instance on each call.."
            }
        };

        this.refresh();
    }

    newResourceFromTemplate(template)
    {
        var r = {};
        angular.copy(template, r);
        var tmp = new this.µJndiDto(r);
        this.resources.push(tmp);
        this.gridApi.selection.selectRow(tmp);
        var ctx = this;
        this.$interval(function ()
        {
            ctx.gridApi.cellNav.scrollToFocus(tmp, ctx.gridOptions.columnDefs[0]);
        }, 0, 1);
    };

    newResource(templateName)
    {
        this.newResourceFromTemplate(resourceTemplates[templateName]);
    }

    savealias()
    {
        this.µJndiDto.save({}, this.selected[0], this.refresh.bind(this));
    };

    refresh()
    {
        this.resources = this.µJndiDto.query();
        this.selected.length = 0;
    };

    removealias()
    {
        var q = this.selected[0];

        q.parameters.length = 0;
        this.selected2.length = 0;

        this.µJndiDto.delete({ id: q.id });

        var ctx = this;
        this.$interval(function ()
        {
            ctx.selected.length = 0;
        }, 0, 1);

        this.resources.splice(this.resources.indexOf(q), 1);
    };

    removeprms()
    {
        var prm = null;
        for (var i = 0; i < this.selected2.length; i++)
        {
            prm = this.selected2[i];
            this.selected[0].parameters.splice(this.selected[0].parameters.indexOf(prm), 1);
        }
    };

    addprm()
    {
        var t = {
            'key': 'name',
            'value': 'value'
        };
        this.selected[0].parameters.push(t);
        this.gridApi2.selection.selectRow(t);
        var ctx = this;
        this.$interval(function ()
        {
            ctx.gridApi2.cellNav.scrollToFocus(t, ctx.gridOptions2.columnDefs[0]);
        }, 0, 1);
    };



}
JndiListCtrl.$inject = ['µJndiDto', 'jqmCellTemplateBoolean', 'jqmCellEditorTemplateBoolean', '$interval'];

export const jndiListComponent = {
    controller: JndiListCtrl,
    template: template,
    bindings: {
        someInput: '<',
        someOutput: '&'
    }
};
