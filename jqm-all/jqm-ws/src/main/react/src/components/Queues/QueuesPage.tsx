import React, { useCallback, useEffect, useRef, useState } from "react";
import { Container, Grid, IconButton, Tooltip } from "@mui/material";
import CircularProgress from "@mui/material/CircularProgress";
import MUIDataTable, { Display, MUIDataTableMeta, SelectableRows } from "mui-datatables";
import HelpIcon from "@mui/icons-material/Help";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddCircleIcon from "@mui/icons-material/AddCircle";
import { useSnackbar } from "notistack";
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
        setPageTitle("Queues");
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const { canUserAccess } = useAuth();

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
            label: "Name*",
            options: {
                hint: "The name of the queue. This name is very important, as it the key used to designate the queue in the different APIs (for example, when submitting an execution request, one may specify by name a queue in which the request will wait). However, it can still be changed - internally, JQM uses an ID, not this name - the impact is only on the clients' side.",
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
                hint: "A free text description that appears in reports",
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
                hint: "The queue used when none is specified. There can only be one default queue.",
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
                noMatch: 'No queues found',
            }
        },
        download: false,
        print: false,
        selectableRows: (canUserAccess(PermissionObjectType.queue, PermissionAction.delete)) ? "multiple" as SelectableRows : "none" as SelectableRows,
        customToolbar: () => {
            return <>
                {canUserAccess(PermissionObjectType.queue, PermissionAction.create) &&
                    <Tooltip title={"Add line"}>
                        <>
                            <IconButton
                                color="default"
                                aria-label={"add"}
                                onClick={() => setShowDialog(true)}
                                size="large">
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
                title="Queues documentation"
                header="These are FIFO (First In First Out) queues in which batch job execution requests will wait."
                descriptionParagraphs={[
                    "On this page, one may change the characteristics of queues. Changes on this page do not require node reboots.",
                ]}
            />
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
