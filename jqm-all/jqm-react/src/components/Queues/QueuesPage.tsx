import React, { useEffect, useState } from "react";
import {
    Container,
    createStyles,
    Grid,
    IconButton,
    Switch,
    Theme,
    Tooltip,
} from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import APIService from "../../utils/APIService";
import MUIDataTable from "mui-datatables";
import makeStyles from "@material-ui/core/styles/makeStyles";
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
import Modal from "@material-ui/core/Modal";
import { CreateQueueModal } from "./CreateQueueModal";

const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        fab: {
            position: "absolute",
            bottom: theme.spacing(2),
            right: theme.spacing(2),
        },
    })
);

const QueuesPage: React.FC = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [queues, setQueues] = useState<any[] | null>();
    const [editingRowId, setEditingRowId] = useState<number | null>(null);
    const [editingLineValues, setEditingLineValues] = useState<any | null>(null);
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);


    useEffect(() => {
        fetchQueues();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const fetchQueues = () => {
        APIService.get("/q")
            .then((response) => {
                setQueues(response);
                setEditingRowId(null);
                setEditingLineValues(null);
            })
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar(
                    "An error occured, please contact support support@enioka.com for help.",
                    {
                        variant: "error",
                        persist: true,
                    }
                );
            });
    };

    const handleCreate = (newQueue: Queue) => {
        if (newQueue) {
            APIService.post("/q", newQueue)
                .then(() => {
                    fetchQueues();
                    enqueueSnackbar(
                        `Successfully created queue: ${newQueue.name}`,
                        {
                            variant: "success",
                        }
                    );
                })
                .catch((reason) => {
                    console.log(reason);
                    enqueueSnackbar(
                        "An error occured, please contact support support@enioka.com for help.",
                        {
                            variant: "error",
                            persist: true,
                        }
                    );
                });
        }
    };

    const deleteQueues = async (queueIds: any[]) => {
        await Promise.all(queueIds.map((id) => APIService.delete("/q/" + id)))
            .then(() => {
                fetchQueues();
                enqueueSnackbar(
                    `Successfully deleted queue${queueIds.length > 1 ? "s" : ""
                    }`,
                    {
                        variant: "success",
                    }
                );
            })
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar(
                    "An error occured, please contact support support@enioka.com for help.",
                    {
                        variant: "error",
                        persist: true,
                    }
                );
            });
    };


    /*
    * Render cell containing boolean value
    */
    const renderBooleanCell = (value: any, tableMeta: any, updateValue: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return <Switch
                checked={editingLineValues[tableMeta.columnIndex]}
                onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                    let values = [...editingLineValues];
                    values[tableMeta.columnIndex] = event.target.checked;
                    setEditingLineValues(values);
                }}
            />
        } else {
            return value ? <DoneIcon /> : <BlockIcon />;
        }
    };

    /**
    * Render cell with action buttons
    */
    const renderActionsCell = (value: any, tableMeta: any, updateValue: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return (<>
                <Tooltip title={"Save changes"}>
                    <IconButton color="default" aria-label={"save"} onClick={() => {
                        saveQueue();
                    }}>
                        <SaveIcon />
                    </IconButton>
                </ Tooltip>
                <Tooltip title={"Cancel changes"}>
                    <IconButton color="default" aria-label={"cancel"} onClick={() => {
                        setEditingRowId(null);
                        setEditingLineValues(null);
                    }}>
                        <CancelIcon />
                    </IconButton>
                </ Tooltip>
            </>)
        } else {
            return (<>
                <Tooltip title={"Edit line"}>
                    <IconButton color="default" aria-label={"edit"} onClick={() => {
                        setEditingRowId(tableMeta.rowIndex)
                        setEditingLineValues(tableMeta.rowData)
                    }}>
                        <CreateIcon />
                    </IconButton>
                </ Tooltip>
                <Tooltip title={"Delete line"}>
                    <IconButton color="default" aria-label={"delete"} onClick={() => {
                        //
                    }}>
                        <DeleteIcon />
                    </IconButton>
                </ Tooltip>
            </>
            );
        }
    }

    /*
    * Render cell containing string value
    * TODO: make cell size rigid
    */
    const renderStringCell = (value: any, tableMeta: any, updateValue: any) => {
        if (editingRowId === tableMeta.rowIndex) {
            return (
                <TextField
                    value={editingLineValues[tableMeta.columnIndex]}
                    onChange={(event: React.ChangeEvent<HTMLInputElement>) => {
                        let values = [...editingLineValues];
                        values[tableMeta.columnIndex] = event.target.value;
                        setEditingLineValues(values);
                    }}
                    fullWidth={true}
                    inputProps={{
                        style: {
                            //textAlign: "center",
                            fontSize: "0.8125rem"
                        }
                    }}
                />
            );
        } else {
            return value;
        }
    };

    const saveQueue = () => {
        const request = {
            "id": editingLineValues[0],
            "name": editingLineValues[1],
            "description": editingLineValues[2],
            "defaultQueue": editingLineValues[3],
        }
        APIService.put("/q/" + request["id"], request)
            .then(() => {
                fetchQueues();
                enqueueSnackbar("Successfully saved queue", {
                    variant: 'success',
                });

            })
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar("An error occured, please contact support for help.", {
                    variant: 'error',
                    persist: true
                })
            })
    }

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                display: "excluded"
            }
        },
        {
            name: "name",
            label: "Name",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderStringCell
            }
        },
        {
            name: "description",
            label: "Description",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderStringCell // TODO: renderTextCell for longer content?
            }
        },
        {
            name: "defaultQueue",
            label: "Is default",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderBooleanCell,
            }
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderActionsCell,
            }
        }
    ]

    const options = {
        download: false,
        print: false,
        //filterType: 'checkbox',
        customToolbar: () => {
            return (
                <>
                    <Tooltip title={"Add line"}>
                        <IconButton
                            color="default"
                            aria-label={"add"}
                            onClick={() => setShowModal(true)}
                        >
                            <AddCircleIcon />
                            <CreateQueueModal
                                showModal={showModal}
                                setShowModal={setShowModal}
                                onCreate={handleCreate}
                            />
                        </IconButton>
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
