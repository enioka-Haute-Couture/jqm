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
    const [editingCellRowId, setEditingCellRowId] = useState<number | null>(
        null
    );
    const classes = useStyles();
    const [showModal, setShowModal] = useState(false);

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

    const fetchQueues = () => {
        APIService.get("/q")
            .then((response) => {
                setQueues(response);
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

    const deleteQueues = async (queueIds: any[]) => {
        await Promise.all(queueIds.map((id) => APIService.delete("/q/" + id)))
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

    useEffect(() => {
        fetchQueues();
    }, []);

    /*
     * Render cell containing boolean value
     */
    const renderBooleanCell = (
        value: any,
        tableMeta: any,
        updateValue: any
    ) => {
        if (editingCellRowId === tableMeta.rowIndex) {
            return (
                <Switch
                //checked={state.checkedA}
                //onChange={handleChange}
                //name="checkedA"
                />
            );
        } else {
            return value ? <DoneIcon /> : <BlockIcon />;
        }
    };

    /**
     * Render cell with action buttons
     */
    const renderActionsCell = (
        value: any,
        tableMeta: any,
        updateValue: any
    ) => {
        if (editingCellRowId === tableMeta.rowIndex) {
            return (
                <>
                    <Tooltip title={"Save changes"}>
                        <IconButton
                            color="default"
                            aria-label={"save"}
                            onClick={(e) => {
                                setEditingCellRowId(null);
                            }}
                        >
                            <SaveIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Cancel changes"}>
                        <IconButton
                            color="default"
                            aria-label={"cancel"}
                            onClick={() => {
                                setEditingCellRowId(null);
                            }}
                        >
                            <CancelIcon />
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
                                setEditingCellRowId(tableMeta.rowIndex);
                            }}
                        >
                            <CreateIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Delete line"}>
                        <IconButton
                            color="default"
                            aria-label={"delete"}
                            onClick={() => deleteQueues([value])}
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
     */
    const renderStringCell = (value: any, tableMeta: any, updateValue: any) => {
        if (editingCellRowId === tableMeta.rowIndex) {
            return (
                <TextField
                    //className={this.props.classes.editedCell}
                    defaultValue={value}
                    //onKeyDown={this.cellInputHandleKeyDown}
                    //onChange={this.cellInputOnChange(this.props.columns[tableMeta.columnIndex].type)}
                    fullWidth={true}
                    //error={this.state.editedValueError}
                    inputProps={{
                        style: {
                            //textAlign: "center",
                            fontSize: "0.8125rem",
                        },
                    }}
                />
            );
        } else {
            return value;
        }
    };

    const columns = [
        {
            name: "name",
            label: "Name",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderStringCell,
            },
        },
        {
            name: "description",
            label: "Description",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderStringCell,
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
            name: "id",
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
