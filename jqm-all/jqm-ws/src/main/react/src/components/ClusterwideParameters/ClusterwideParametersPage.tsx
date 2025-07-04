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
import { HelpDialog } from "../HelpDialog";
import { setPageTitle } from "../../utils/title";

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
        setPageTitle("Cluster-wide parameters");
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

    const [isHelpModalOpen, setIsHelpModalOpen] = useState(false);

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
                hint: "The parameter key. Not necessarily unique.",
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
                hint: "The value of the parameter",
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
                filter: false,
                sort: false,
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
        textLabels: {
            body: {
                noMatch: 'No parameters found',
            }
        },
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.prm, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.prm, PermissionAction.create) &&
                <>
                    <Tooltip title={"Add line"}>
                            <IconButton
                                color="default"
                                aria-label={"add"}
                                onClick={() => setShowDialog(true)}
                                size="large">
                                <AddCircleIcon />
                            </IconButton>
                            </Tooltip>

                    <Tooltip title={"Add line dialog"}>
                            <CreateParameterDialog
                                showDialog={showDialog}
                                closeDialog={() => setShowDialog(false)}
                                createParameter={createParameter}
                            />
                    </Tooltip>
                    </>
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
                    <IconButton color="default" aria-label={"help"} size="large" onClick={() => setIsHelpModalOpen(true)}>
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
            <HelpDialog
                isOpen={isHelpModalOpen}
                onClose={() => setIsHelpModalOpen(false)}
                title="Cluster-wide parameters documentation"
                header="These parameters apply to every node inside the cluster."
                descriptionParagraphs={[
                    "On this page, one may change the global cluster parameters. Please see the full documentation for the parameters. The need to reboot after a change depends on the parameter."
                ]}
            />
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
