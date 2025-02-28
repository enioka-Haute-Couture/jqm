import React, { useCallback, useEffect, useRef, useState } from "react";
import { Container, Grid, IconButton, Tooltip } from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import { useClassLoaderAPI } from "./ClassLoaderAPI";
import { CreateClassLoaderDialog } from "./CreateClassLoaderDialog";
import { EditHiddenClassesDialog } from "./EditHiddenClassesDialog";
import { EditAllowedRunnersDialog } from "./EditAllowedRunnersDialog";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";
import { setPageTitle } from "../../utils/title";
import { renderActionsCell, renderBooleanCell, renderInputCell } from "../TableCells";
import { renderDialogCell } from "../TableCells/renderDialogCell";

const ClassLoadersPage: React.FC = () => {
    const [showDialog, setShowDialog] = useState(false);
    const [editHiddenClassesClId, setEditHiddenClassesClId] = useState<
        string | null
    >(null);
    const [editAllowedRunnersClId, setEditAllowedRunnersClId] = useState<
        string | null
    >(null);


    const [editingRowId, setEditingRowId] = useState<number | null>(null);

    const [childFirst, setChildFirst] = useState<boolean>(false);
    const [hiddenClasses, setHiddenClasses] = useState<string>("");
    const [tracingEnabled, setTracingEnabled] = useState<boolean>(false);
    const [persistent, setPersistent] = useState<boolean>(false);
    const [allowedRunners, setAllowedRunners] = useState<string>("");

    const nameInputRef = useRef(null);

    const { classLoaders, fetchClassLoaders, createClassLoader, updateClassLoader, deleteClassLoaders } =
        useClassLoaderAPI();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.cl, PermissionAction.read)) {
            fetchClassLoaders();
        }
        setPageTitle("Class Loaders");
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const { canUserAccess } = useAuth();

    const handleOnDelete = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [classLoaderId] = tableMeta.rowData;
            deleteClassLoaders([classLoaderId]);
        },
        [deleteClassLoaders]
    );


    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [classLoaderId] = tableMeta.rowData;
            const { value: name } = nameInputRef.current!;

            if (name) {
                updateClassLoader({
                    id: classLoaderId,
                    name,
                    childFirst,
                    hiddenClasses,
                    tracingEnabled,
                    persistent,
                    allowedRunners,
                }).then(() => setEditingRowId(null));
            }
        },
        [childFirst, hiddenClasses, persistent, tracingEnabled, updateClassLoader, allowedRunners]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);
    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setEditingRowId(tableMeta.rowIndex);
        setHiddenClasses(tableMeta.rowData[3]);
        setAllowedRunners(tableMeta.rowData[6]);
    }, []);


    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded" as Display,
            },
        },
        {
            name: "name",
            label: "Name*",
            options: {
                hint: "The key used to identify the class loader in the deployment descriptor. Unique.",
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    nameInputRef,
                    editingRowId,
                    true
                ),
            },
        },
        {
            name: "childFirst",
            label: "Child First",
            options: {
                hint: "Offer option to have child first class loading. Parent firstis the norm in JSE.",
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    childFirst,
                    setChildFirst
                ),
            },
        },
        {
            name: "hiddenClasses",
            label: "Hidden Classes",
            options: {
                hint: 'Offer possibility to hide Java classes from jobs. One or more regex defining classes never to load from the parent class loader.',
                filter: true,
                sort: true,
                customBodyRender: renderDialogCell(
                    editingRowId,
                    "Click to edit hidden classes",
                    hiddenClasses,
                    (value: string) => value,
                    setEditHiddenClassesClId
                ),
            },
        },
        {
            name: "tracingEnabled",
            label: "Tracing Enabled",
            options: {
                hint: "Activate listing all class loaded inside the job log",
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    tracingEnabled,
                    setTracingEnabled
                ),
            },
        },
        {
            name: "persistent",
            label: "Persistent",
            options: {
                hint: 'When false, the class loader is transient: it is created to run a new job instance and is thrown out when the job instance ends. When true, it is not thrown out at the end and will be reused by all job instances created from the different job definitions using this class loader(therefore, multiple job definitions can share the same static context).',
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    persistent,
                    setPersistent
                ),
            },
        },
        {
            name: "allowedRunners",
            label: "Allowed Runners",
            options: {
                hint: 'The different runners that are active in this context. If not set, the global parameter job_runners is used instead.',
                filter: true,
                sort: true,
                customBodyRender: renderDialogCell(
                    editingRowId,
                    "Click to edit allowed runners",
                    allowedRunners,
                    (value: string) => value,
                    setEditAllowedRunnersClId
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
                    canUserAccess(PermissionObjectType.queue, PermissionAction.update),
                    canUserAccess(PermissionObjectType.queue, PermissionAction.delete)
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: {
            body: {
                noMatch: 'No class loaders found',
            }
        },
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.cl, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.cl, PermissionAction.create) &&
                    <Tooltip title={"Add line"}>
                        <>
                            <IconButton
                                color="default"
                                aria-label={"add"}
                                onClick={() => setShowDialog(true)}
                                size="large">
                                <AddCircleIcon />
                            </IconButton>
                            <CreateClassLoaderDialog
                                showDialog={showDialog}
                                closeDialog={() => setShowDialog(false)}
                                createClassLoader={createClassLoader}
                            />
                        </>
                    </Tooltip>}
                <Tooltip title={"Refresh"}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => fetchClassLoaders()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },
        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const classLoadersIds: number[] = [];
            data.forEach(({ index }) => {
                const classLoader = classLoaders ? classLoaders[index] : null;
                if (classLoader) {
                    classLoadersIds.push(classLoader.id!);
                }
            });
            deleteClassLoaders(classLoadersIds);
        },
    };

    if (!canUserAccess(PermissionObjectType.queue, PermissionAction.read)) {
        return <AccessForbiddenPage />
    }


    return classLoaders ? (
        <Container maxWidth={false}>
            <MUIDataTable
                title={"Class loaders"}
                data={classLoaders}
                columns={columns}
                options={options}
            />
            {editHiddenClassesClId !== null && (
                <EditHiddenClassesDialog
                    closeDialog={() => setEditHiddenClassesClId(null)}
                    hiddenClasses={hiddenClasses !== "" ? hiddenClasses.split(',') : []}
                    setHiddenClasses={(hiddenClasses: string[]) =>
                        setHiddenClasses(hiddenClasses.join(','))
                    }
                />
            )}
            {editAllowedRunnersClId !== null && (
                <EditAllowedRunnersDialog
                    closeDialog={() => setEditAllowedRunnersClId(null)}
                    allowedRunners={allowedRunners !== "" ? allowedRunners.split(',') : []}
                    setAllowedRunners={(allowedRunners: string[]) =>
                        setAllowedRunners(allowedRunners.join(','))
                    }
                />
            )}
        </Container>
    ) : (
        <Grid container justifyContent="center">
            <CircularProgress />
        </Grid>
    );
};

export default ClassLoadersPage;
