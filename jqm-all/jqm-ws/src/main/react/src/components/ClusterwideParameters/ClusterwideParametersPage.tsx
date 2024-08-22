import React, { useCallback, useEffect, useRef, useState } from "react";
import { Container, Grid, IconButton, Tooltip } from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import { CreateParameterDialog } from "./CreateParameterDialog";
import useParametersApi from "./ParametersApi";
import { renderActionsCell, renderInputCell } from "../TableCells";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";

const ClusterwideParametersPage: React.FC = () => {
    const [showDialog, setShowDialog] = useState(false);
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const paramKeyInputRef = useRef(null);
    const paramValueInputRef = useRef(null);

    const {
        parameters,
        fetchParameters,
        createParameter,
        updateParameter,
        deleteParameter,
    } = useParametersApi();

    const { canUserAccess } = useAuth();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.prm, PermissionAction.read)) {
            fetchParameters();
        }
    }, [fetchParameters, canUserAccess]);

    const handleOnDelete = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [paramId] = tableMeta.rowData;
            deleteParameter([paramId]);
        },
        [deleteParameter]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [paramId] = tableMeta.rowData;
            const { value: key } = paramKeyInputRef.current!;
            const { value } = paramValueInputRef.current!;
            if (paramId && (key || value)) {
                updateParameter({ id: paramId, key: key, value: value }).then(
                    () => setEditingRowId(null)
                );
            }
        },
        [updateParameter]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);
    const handleOnEdit = useCallback(
        (tableMeta: MUIDataTableMeta) => setEditingRowId(tableMeta.rowIndex),
        []
    );

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded" as Display,
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
                    handleOnEdit,
                    canUserAccess(PermissionObjectType.prm, PermissionAction.update),
                    canUserAccess(PermissionObjectType.prm, PermissionAction.delete)
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.prm, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.prm, PermissionAction.create) &&
                    <Tooltip title={"Add line"}>
                        <>
                            <IconButton
                                color="default"
                                aria-label={"add"}
                                onClick={() => setShowDialog(true)}
                                size="large">
                                <AddCircleIcon />
                            </IconButton>
                            <CreateParameterDialog
                                showDialog={showDialog}
                                closeDialog={() => setShowDialog(false)}
                                createParameter={createParameter}
                            />
                        </>
                    </Tooltip>
                }
                <Tooltip title={"Refresh"}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => fetchParameters()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={"Help"}>
                    <IconButton color="default" aria-label={"help"} size="large">
                        <HelpIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },
        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const paramIds: number[] = [];
            data.forEach(({ index }) => {
                const parameter = parameters ? parameters[index] : null;
                if (parameter) {
                    paramIds.push(parameter.id!);
                }
            });
            deleteParameter(paramIds);
        },
    };

    if (!canUserAccess(PermissionObjectType.prm, PermissionAction.read)) {
        return <AccessForbiddenPage />
    }

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
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};

export default ClusterwideParametersPage;
