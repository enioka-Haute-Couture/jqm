import React, { useCallback, useEffect, useRef, useState } from "react";
import { Container, Grid, IconButton, Tooltip } from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import { useTranslation } from "react-i18next";
import { useMUIDataTableTextLabels } from "../../utils/useMUIDataTableTextLabels";
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
    const { t } = useTranslation();
    const muiTableTextLabels = useMUIDataTableTextLabels(t("classLoaders.noMatch"));
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

    const { canUserAccess } = useAuth();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.cl, PermissionAction.read)) {
            fetchClassLoaders();
        }
        setPageTitle(t("classLoaders.title"));
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [canUserAccess, t]);


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
        setChildFirst(tableMeta.rowData[2]);
        setHiddenClasses(tableMeta.rowData[3]);
        setTracingEnabled(tableMeta.rowData[4]);
        setPersistent(tableMeta.rowData[5]);
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
            label: t("classLoaders.name"),
            options: {
                hint: t("classLoaders.hints.name"),
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
            label: t("classLoaders.childFirst"),
            options: {
                hint: t("classLoaders.hints.childFirst"),
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
            label: t("classLoaders.hiddenClasses"),
            options: {
                hint: t("classLoaders.hints.hiddenClasses"),
                filter: true,
                sort: true,
                customBodyRender: renderDialogCell(
                    editingRowId,
                    t("classLoaders.clickToEditHiddenClasses"),
                    hiddenClasses,
                    (value: string) => value,
                    setEditHiddenClassesClId
                ),
            },
        },
        {
            name: "tracingEnabled",
            label: t("classLoaders.tracingEnabled"),
            options: {
                hint: t("classLoaders.hints.tracingEnabled"),
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
            label: t("classLoaders.persistent"),
            options: {
                hint: t("classLoaders.hints.persistent"),
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
            label: t("classLoaders.allowedRunners"),
            options: {
                hint: t("classLoaders.hints.allowedRunners"),
                filter: true,
                sort: true,
                customBodyRender: renderDialogCell(
                    editingRowId,
                    t("classLoaders.clickToEditAllowedRunners"),
                    allowedRunners,
                    (value: string) => value,
                    setEditAllowedRunnersClId
                ),
            },
        },
        {
            name: "",
            label: t("common.actions"),
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
                    canUserAccess(PermissionObjectType.queue, PermissionAction.delete),
                    [],
                    t
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        textLabels: muiTableTextLabels,
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.cl, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.cl, PermissionAction.create) &&
                    <>
                        <Tooltip title={t("common.add")}>

                            <IconButton
                                color="default"
                                aria-label={"add"}
                                onClick={() => setShowDialog(true)}
                                size="large">
                                <AddCircleIcon />
                            </IconButton>
                        </Tooltip>
                        <Tooltip title={t("common.add")}>
                            <CreateClassLoaderDialog
                                showDialog={showDialog}
                                closeDialog={() => setShowDialog(false)}
                                createClassLoader={createClassLoader}
                            />
                        </Tooltip>
                    </>}
                <Tooltip title={t("common.refresh")}>
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
                title={t("classLoaders.title")}
                data={classLoaders}
                columns={columns}
                options={options}
            />
            {editHiddenClassesClId !== null && (
                <EditHiddenClassesDialog
                    closeDialog={() => setEditHiddenClassesClId(null)}
                    hiddenClasses={hiddenClasses ? hiddenClasses.split(',') : []}
                    setHiddenClasses={(hiddenClasses: string[]) =>
                        setHiddenClasses(hiddenClasses.join(','))
                    }
                />
            )}
            {editAllowedRunnersClId !== null && (
                <EditAllowedRunnersDialog
                    closeDialog={() => setEditAllowedRunnersClId(null)}
                    allowedRunners={allowedRunners ? allowedRunners.split(',') : []}
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
