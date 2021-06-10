import React, { useEffect, useState, useCallback, useRef } from "react";
import { Container, Grid, IconButton, Tooltip } from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable from "mui-datatables";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import { renderInputCell, renderActionsCell } from "../TableCells";
import { CreateParameterDialog } from "./CreateParameterDialog";
import useParametersApi from "./ParametersApi";
import { Parameter } from "./Parameter";

const ClusterwideParametersPage: React.FC = () => {
    const [showDialog, setShowDialog] = useState(false);
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const paramKeyInputRef = useRef(null);
    const paramValueInputRef = useRef(null);

    const {
        parameters,
        fetchParameters,
        createParameter,
        updateParameters,
        deleteParameter,
    } = useParametersApi();

    useEffect(() => {
        fetchParameters();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const handleOnDelete = useCallback(
        (tableMeta) => {
            const [paramId] = tableMeta.rowData;
            deleteParameter([paramId]);
        },
        [deleteParameter]
    );

    const handleOnSave = useCallback(
        (tableMeta) => {
            console.log(tableMeta);
            const [paramId] = tableMeta.rowData;
            const { value: key } = paramKeyInputRef.current!;
            const { value } = paramValueInputRef.current!;

            const updatedParamsList = tableMeta.currentTableData.map(
                ({ data }: { data: any[] }) => {
                    if (data[0] === paramId) {
                        return { id: paramId, key: key, value: value };
                    }
                    return { id: data[0], key: data[1], value: data[2] };
                }
            );
            if (updatedParamsList) {
                updateParameters(updatedParamsList).then(() =>
                    setEditingRowId(null)
                );
            }
        },
        [updateParameters]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);
    const handleOnEdit = useCallback(
        (tableMeta) => setEditingRowId(tableMeta.rowIndex),
        []
    );

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded",
            },
        },
        {
            name: "key",
            label: "Key",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    paramKeyInputRef,
                    editingRowId
                ),
            },
        },
        {
            name: "value",
            label: "Value",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    paramValueInputRef,
                    editingRowId
                ),
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderActionsCell(
                    handleOnCancel,
                    handleOnSave,
                    handleOnDelete,
                    editingRowId,
                    handleOnEdit
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        download: false,
        print: false,
        customToolbar: () => {
            return (
                <>
                    <Tooltip title={"Add line"}>
                        <>
                            <IconButton
                                color="default"
                                aria-label={"add"}
                                onClick={() => setShowDialog(true)}
                            >
                                <AddCircleIcon />
                            </IconButton>
                            <CreateParameterDialog
                                showDialog={showDialog}
                                closeDialog={() => setShowDialog(false)}
                                createParameter={createParameter}
                            />
                        </>
                    </Tooltip>
                    <Tooltip title={"Refresh"}>
                        <IconButton
                            color="default"
                            aria-label={"refresh"}
                            onClick={() => fetchParameters()}
                        >
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Help"}>
                        <IconButton color="default" aria-label={"help"}>
                            <HelpIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        },
        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const paramIds: number[] = [];
            data.forEach(({ index }) => {
                const queue = parameters ? parameters[index] : null;
                if (queue) {
                    paramIds.push(queue.id!);
                }
            });
            deleteParameter(paramIds);
        },
    };

    return parameters ? (
        <Container maxWidth={false}>
            <MUIDataTable
                title={"Cluster-wide parameters"}
                data={parameters}
                columns={columns}
                options={options}
            />
        </Container>
    ) : (
        <Grid container justify="center">
            <CircularProgress />
        </Grid>
    );
};

export default ClusterwideParametersPage;
