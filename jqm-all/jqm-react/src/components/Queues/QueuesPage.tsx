import React, { useEffect, useState, useCallback, useRef } from "react";
import { Container, Grid, IconButton, Tooltip } from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable from "mui-datatables";
import DeleteIcon from "@material-ui/icons/Delete";
import CreateIcon from "@material-ui/icons/Create";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";
import useQueueCrudApi from "./useQueueCrudApi";
import { CreateQueueModal } from "./CreateQueueModal";
import { renderStringCell, renderBooleanCell } from "../TableCells";

const QueuesPage: React.FC = () => {
    const [showModal, setShowModal] = useState(false);
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const [editingDefaultQueue, setEditingDefaultQueue] = useState<any | false>(
        false
    );
    const editingDescriptionInputRef = useRef(null);
    const editingQueueNameInputRef = useRef(null);

    const {
        queues,
        fetchQueues,
        createQueue,
        updateQueue,
        deleteQueues,
    } = useQueueCrudApi();

    useEffect(() => {
        fetchQueues();
    }, []);

    const updateRow = useCallback(
        (queueId: number) => {
            const { value: queueName } = editingQueueNameInputRef.current!;
            const { value: description } = editingDescriptionInputRef.current!;
            if (queueId && queueName && description) {
                updateQueue({
                    id: queueId,
                    name: queueName,
                    description: description,
                    defaultQueue: editingDefaultQueue
                }
                ).then(() => setEditingRowId(null));
            }
        },
        [updateQueue, editingDefaultQueue]
    );

    /**
     * Render cell with action buttons
     */
    const renderActionsCell = (value: any, tableMeta: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            const queueId = tableMeta.rowData ? tableMeta.rowData[0] : null;
            return (
                <>
                    <Tooltip title={"Cancel changes"}>
                        <IconButton
                            color="default"
                            aria-label={"cancel"}
                            onClick={() => setEditingRowId(null)} // TODO: how to fix cancel ?
                        >
                            <CancelIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Save changes"}>
                        <IconButton
                            color="default"
                            aria-label={"save"}
                            onClick={() => updateRow(queueId)}
                        >
                            <SaveIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        } else {
            return (
                <>
                    <Tooltip title={"Edit line"}>
                        <IconButton
                            color="default"
                            aria-label={"edit"}
                            onClick={() => {
                                setEditingRowId(tableMeta.rowIndex);
                                setEditingDefaultQueue(tableMeta.rowData[3]);
                            }}
                        >
                            <CreateIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Delete line"}>
                        <IconButton
                            color="default"
                            aria-label={"delete"}
                            onClick={() => {
                                const [queueId] = tableMeta.rowData;
                                deleteQueues([queueId]);
                            }}
                        >
                            <DeleteIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        }
    };

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded",
            },
        },
        {
            name: "name",
            label: "Name",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderStringCell(
                    editingQueueNameInputRef,
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
                customBodyRender: renderStringCell(
                    editingDescriptionInputRef,
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
                    editingDefaultQueue,
                    setEditingDefaultQueue
                ),
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderActionsCell,
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
                                onClick={() => setShowModal(true)}
                            >
                                <AddCircleIcon />
                            </IconButton>
                            <CreateQueueModal
                                showModal={showModal}
                                closeModal={() => setShowModal(false)}
                                createQueue={createQueue}
                            />
                        </>
                    </Tooltip>
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
            const queueIds = data.map(({ index }) => {
                const queue = queues ? queues[index] : null;
                return queue ? queue.id : null;
            });
            deleteQueues(queueIds);
        },
    };

    return queues ? (
        <Container>
            <MUIDataTable
                title={"Queues"}
                data={queues}
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

export default QueuesPage;
