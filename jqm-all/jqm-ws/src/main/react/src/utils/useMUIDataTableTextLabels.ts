import { useTranslation } from "react-i18next";
import { MUIDataTableColumn } from "mui-datatables";

export const useMUIDataTableTextLabels = (customNoMatch?: string) => {
    const { t } = useTranslation();

    return {
        body: {
            noMatch: customNoMatch || t("muiDatatable.body.noMatch"),
            toolTip: t("muiDatatable.body.toolTip"),
            columnHeaderTooltip: (column: MUIDataTableColumn) =>
                t("muiDatatable.body.columnHeaderTooltip", {
                    column: column.label || "",
                }),
        },
        pagination: {
            next: t("muiDatatable.pagination.next"),
            previous: t("muiDatatable.pagination.previous"),
            rowsPerPage: t("muiDatatable.pagination.rowsPerPage"),
            displayRows: t("muiDatatable.pagination.displayRows"),
        },
        toolbar: {
            search: t("muiDatatable.toolbar.search"),
            downloadCsv: t("muiDatatable.toolbar.downloadCsv"),
            print: t("muiDatatable.toolbar.print"),
            viewColumns: t("muiDatatable.toolbar.viewColumns"),
            filterTable: t("muiDatatable.toolbar.filterTable"),
        },
        filter: {
            all: t("muiDatatable.filter.all"),
            title: t("muiDatatable.filter.title"),
            reset: t("muiDatatable.filter.reset"),
        },
        viewColumns: {
            title: t("muiDatatable.viewColumns.title"),
            titleAria: t("muiDatatable.viewColumns.titleAria"),
        },
        selectedRows: {
            text: t("muiDatatable.selectedRows.text"),
            delete: t("muiDatatable.selectedRows.delete"),
            deleteAria: t("muiDatatable.selectedRows.deleteAria"),
        },
    };
};
