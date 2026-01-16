import React, { useCallback, useEffect, useRef, useState } from "react";
import { Container, Grid, IconButton, Tooltip } from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import { useSnackbar } from "notistack";
import { useTranslation } from "react-i18next";
import { CreateQueueDialog } from "./CreateQueueDialog";
import useQueueAPI from "./QueueAPI";
import {
    renderActionsCell,
    renderBooleanCell,
    renderInputCell,
} from "../TableCells";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";
import { HelpDialog } from "../HelpDialog";
import { setPageTitle } from "../../utils/title";
import { useMUIDataTableTextLabels } from "../../utils/useMUIDataTableTextLabels";

const QueuesPage: React.FC = () => {
    const { t } = useTranslation();
    const muiTextLabels = useMUIDataTableTextLabels(t("queues.noMatch"));
    const [showDialog, setShowDialog] = useState(false);
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const [defaultQueue, setDefaultQueue] = useState<boolean>(false);
    const descriptionInputRef = useRef(null);
    const queueNameInputRef = useRef(null);

    const { queues, fetchQueues, createQueue, updateQueue, deleteQueues } =
        useQueueAPI();

    const { enqueueSnackbar } = useSnackbar();

    const { canUserAccess } = useAuth();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.queue, PermissionAction.read)) {
            fetchQueues();
        }
        setPageTitle("Queues");
    }, [canUserAccess]);

    const handleOnDelete = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [queueId] = tableMeta.rowData;
            deleteQueues([queueId]);
        },
        [deleteQueues]
    );

    const handleOnSave = useCallback(
        (tableMeta: MUIDataTableMeta) => {
            const [queueId] = tableMeta.rowData;
            const { value: name } = queueNameInputRef.current!;
            const { value: description } = descriptionInputRef.current!;

            if (defaultQueue && queues?.some(q => q.defaultQueue)) {
                enqueueSnackbar(t("queues.messages.onlyOneDefault"), {
                    variant: "warning",
                });
                return;
            }
            if (queueId && name) {
                updateQueue({
                    id: queueId,
                    name,
                    description,
                    defaultQueue: defaultQueue,
                }).then(() => setEditingRowId(null));
            }
        },
        [updateQueue, defaultQueue, queues, enqueueSnackbar]
    );

    const handleOnCancel = useCallback(() => setEditingRowId(null), []);
    const handleOnEdit = useCallback((tableMeta: MUIDataTableMeta) => {
        setDefaultQueue(tableMeta.rowData[3]);
        setEditingRowId(tableMeta.rowIndex);
    }, []);

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
            name: "name",
            label: t("queues.name"),
            options: {
                hint: t("queues.hints.name"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    queueNameInputRef,
                    editingRowId
                ),
            },
        },
        {
            name: "description",
            label: t("queues.description"),
            options: {
                hint: t("queues.hints.description"),
                filter: true,
                sort: true,
                customBodyRender: renderInputCell(
                    descriptionInputRef,
                    editingRowId
                ),
            },
        },
        {
            name: "defaultQueue",
            label: t("queues.isDefault"),
            options: {
                hint: t("queues.hints.isDefault"),
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell(
                    editingRowId,
                    defaultQueue,
                    setDefaultQueue
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
        textLabels: muiTextLabels,
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.queue, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.queue, PermissionAction.create) &&
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
                        <CreateQueueDialog
                            showDialog={showDialog}
                            closeDialog={() => setShowDialog(false)}
                            createQueue={createQueue}
                            canBeDefaultQueue={queues ? !queues.some(q => q.defaultQueue) : true}
                        />
                    </>}
                <Tooltip title={t("common.refresh")}>
                    <IconButton
                        color="default"
                        aria-label={"refresh"}
                        onClick={() => fetchQueues()}
                        size="large">
                        <RefreshIcon />
                    </IconButton>
                </Tooltip>
                <Tooltip title={t("common.help")}>
                    <IconButton color="default" aria-label={"help"} size="large" onClick={() => setIsHelpModalOpen(true)}>
                        <HelpIcon />
                    </IconButton>
                </Tooltip>
            </>;
        },
        onRowsDelete: ({ data }: { data: any[] }) => {
            // delete all rows by index
            const queueIds: number[] = [];
            data.forEach(({ index }) => {
                const queue = queues ? queues[index] : null;
                if (queue) {
                    queueIds.push(queue.id!);
                }
            });
            deleteQueues(queueIds);
        },
    };

    if (!canUserAccess(PermissionObjectType.queue, PermissionAction.read)) {
        return <AccessForbiddenPage />
    }

    return queues ? (
        <Container maxWidth={false}>
            <HelpDialog
                isOpen={isHelpModalOpen}
                onClose={() => setIsHelpModalOpen(false)}
                title={t("queues.documentation.title")}
                header={t("queues.documentation.header")}
                descriptionParagraphs={[
                    t("queues.documentation.description"),
                ]}
            />
            <MUIDataTable
                title={t("queues.title")}
                data={queues}
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

export default QueuesPage;
