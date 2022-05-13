import React, { useEffect, useState, useCallback, useRef } from "react";
import { Container, Grid, IconButton, Tooltip } from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable, { Display, SelectableRows } from "mui-datatables";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import {
    renderInputCell,
    renderBooleanCell,
    renderActionsCell,
} from "../TableCells";
import { CreateQueueDialog } from "./CreateQueueDialog";
import useQueueAPI from "./QueueAPI";
import { PermissionAction, PermissionObjectType, useAuth } from "../../utils/AuthService";
import AccessForbiddenPage from "../AccessForbiddenPage";
import { useSnackbar } from "notistack";

const QueuesPage: React.FC = () => {
    const [showDialog, setShowDialog] = useState(false);
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const [defaultQueue, setDefaultQueue] = useState<boolean>(false);
    const descriptionInputRef = useRef(null);
    const queueNameInputRef = useRef(null);

    const { queues, fetchQueues, createQueue, updateQueue, deleteQueues } =
        useQueueAPI();

    const { enqueueSnackbar } = useSnackbar();

    useEffect(() => {
        if (canUserAccess(PermissionObjectType.queue, PermissionAction.read)) {
            fetchQueues();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const { canUserAccess } = useAuth();

    const handleOnDelete = useCallback(
        (tableMeta) => {
            const [queueId] = tableMeta.rowData;
            deleteQueues([queueId]);
        },
        [deleteQueues]
    );

    const handleOnSave = useCallback(
        (tableMeta) => {
            const [queueId] = tableMeta.rowData;
            const { value: name } = queueNameInputRef.current!;
            const { value: description } = descriptionInputRef.current!;

            if (defaultQueue && queues?.some(q => q.defaultQueue)) {
                enqueueSnackbar("Only one default queue can be set", {
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
    const handleOnEdit = useCallback((tableMeta) => {
        setDefaultQueue(tableMeta.rowData[3]);
        setEditingRowId(tableMeta.rowIndex);
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
            label: "Description",
            options: {
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
            label: "Is default",
            options: {
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
                    canUserAccess(PermissionObjectType.queue, PermissionAction.update),
                    canUserAccess(PermissionObjectType.queue, PermissionAction.delete)
                ),
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.queue, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return (
                <>
                    {canUserAccess(PermissionObjectType.queue, PermissionAction.create) &&
                        <Tooltip title={"Add line"}>
                            <>
                                <IconButton
                                    color="default"
                                    aria-label={"add"}
                                    onClick={() => setShowDialog(true)}
                                >
                                    <AddCircleIcon />
                                </IconButton>
                                <CreateQueueDialog
                                    showDialog={showDialog}
                                    closeDialog={() => setShowDialog(false)}
                                    createQueue={createQueue}
                                    canBeDefaultQueue={queues ? !queues.some(q => q.defaultQueue) : true}
                                />
                            </>
                        </Tooltip>}
                    <Tooltip title={"Refresh"}>
                        <IconButton
                            color="default"
                            aria-label={"refresh"}
                            onClick={() => fetchQueues()}
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
            <MUIDataTable
                title={"Queues"}
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
