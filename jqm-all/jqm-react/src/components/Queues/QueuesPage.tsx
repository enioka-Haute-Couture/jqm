import React, { useEffect, useState, useCallback, useRef } from "react";
import {
    Container,
    Grid,
    IconButton,
    Switch,
    Tooltip,
} from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import APIService from "../../utils/APIService";
import MUIDataTable from "mui-datatables";
import DoneIcon from "@material-ui/icons/Done";
import BlockIcon from "@material-ui/icons/Block";
import DeleteIcon from "@material-ui/icons/Delete";
import CreateIcon from "@material-ui/icons/Create";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";
import { useSnackbar } from "notistack";
import TextField from "@material-ui/core/TextField/TextField";
import { Queue } from "./Queue";
import { CreateQueueModal } from "./CreateQueueModal";

const QueuesPage: React.FC = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [showModal, setShowModal] = useState(false);
    const [queues, setQueues] = useState<any[] | null>();
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const [editingDefaultQueue, setEditingDefaultQueue] = useState<any | false>(
        false
    );
    const editingDescriptionInputRef = useRef(null);
    const editingQueueNameInputRef = useRef(null);

    useEffect(() => {
        fetchQueues();
    }, []);

    const fetchQueues = useCallback(() => {
        APIService.get("/q")
            .then((response) => {
                setQueues(response);
                setEditingRowId(null);
            })
            .catch((reason) => {
                enqueueSnackbar(
                    "An error occured, please contact support support@enioka.com for help.",
                    {
                        variant: "error",
                        persist: true,
                    }
                );
            });
    }, [enqueueSnackbar]);

    const createQueue = useCallback(
        (newQueue: Queue) => {
            APIService.post("/q", newQueue)
                .then(() => {
                    setShowModal(false);
                    fetchQueues();
                    enqueueSnackbar(
                        `Successfully created queue: ${newQueue.name}`,
                        {
                            variant: "success",
                        }
                    );
                })
                .catch((reason) => {
                    enqueueSnackbar(
                        "An error occured, please contact support support@enioka.com for help.",
                        {
                            variant: "error",
                            persist: true,
                        }
                    );
                });
        },
        [enqueueSnackbar, fetchQueues]
    );

    const deleteQueues = useCallback(
        async (queueIds: any[]) => {
            await Promise.all(
                queueIds.map((id) => APIService.delete("/q/" + id))
            )
                .then(() => {
                    fetchQueues();
                    enqueueSnackbar(
                        `Successfully deleted queue${
                            queueIds.length > 1 ? "s" : ""
                        }`,
                        {
                            variant: "success",
                        }
                    );
                })
                .catch((reason) => {
                    enqueueSnackbar(
                        "An error occured, please contact support support@enioka.com for help.",
                        {
                            variant: "error",
                            persist: true,
                        }
                    );
                });
        },
        [enqueueSnackbar, fetchQueues]
    );

    const updateQueue = useCallback(
        (
            queueId: Number,
            queueName: string,
            queueDescription: string,
            defaultQueue: Boolean
        ) => {
            const request: Queue = {
                id: queueId,
                name: queueName,
                description: queueDescription,
                defaultQueue: defaultQueue,
            };
            APIService.put("/q/" + request["id"], request)
                .then(() => {
                    fetchQueues();
                    enqueueSnackbar("Successfully saved queue", {
                        variant: "success",
                    });
                })
                .catch((reason) => {
                    enqueueSnackbar(
                        "An error occured, please contact support for help.",
                        {
                            variant: "error",
                            persist: true,
                        }
                    );
                });
        },
        [fetchQueues, enqueueSnackbar]
    );

    const updateRow = useCallback(
        (queueId: number) => {
            const { value: queueName } = editingQueueNameInputRef.current!;
            const { value: description } = editingDescriptionInputRef.current!;
            if (queueId && queueName && description) {
                updateQueue(
                    queueId,
                    queueName,
                    description,
                    editingDefaultQueue
                );
            }
        },
        [updateQueue, editingDefaultQueue]
    );

    /*
     * Render cell containing boolean value
     */
    const renderBooleanCell = (value: any, tableMeta: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return (
                <Switch
                    checked={editingDefaultQueue}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) =>
                        setEditingDefaultQueue(event.target.checked)
                    }
                />
            );
        } else {
            return value ? <DoneIcon /> : <BlockIcon />;
        }
    };

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
                            onClick={() => setEditingRowId(null)}
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
                            onClick={(e) => {
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

    /*
     * Render cell containing string value
     * TODO: make cell size rigid
     */
    const renderStringCell = (inputRef: any, rowIndex: number) => (
        value: any,
        tableMeta: any
    ) => {
        if (editingRowId === tableMeta.rowIndex) {
            const defaultDescription = tableMeta.rowData
                ? tableMeta.rowData[rowIndex]
                : "";
            return (
                <TextField
                    defaultValue={defaultDescription}
                    inputRef={inputRef}
                    fullWidth={true}
                    inputProps={{
                        style: { fontSize: "0.8125rem" },
                    }}
                />
            );
        } else {
            return value;
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
                customBodyRender: renderStringCell(editingQueueNameInputRef, 1),
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
                    2
                ), // TODO: renderTextCell for longer content?
            },
        },
        {
            name: "defaultQueue",
            label: "Is default",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell,
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
        download: false,
        print: false,
        //filterType: 'checkbox',
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
                        <IconButton
                            color="default"
                            aria-label={"help"}
                            onClick={() => {
                                //
                            }}
                        >
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
        //filterType: 'checkbox',
    };

    if (queues) {
        return (
            <Container>
                <MUIDataTable
                    title={"Queues"}
                    data={queues}
                    columns={columns}
                    options={options}
                />
            </Container>
        );
    } else {
        return (
            <Grid container justify="center">
                <CircularProgress />
            </Grid>
        );
    }
};

export default QueuesPage;
