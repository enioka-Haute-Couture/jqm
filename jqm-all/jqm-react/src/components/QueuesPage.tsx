import React, { useEffect, useState } from 'react';
import { Container, createStyles, Grid, IconButton, Switch, Theme, Tooltip } from '@material-ui/core';
import CircularProgress from "@material-ui/core/CircularProgress";
import APIService from '../utils/APIService';
import MUIDataTable from "mui-datatables";
import makeStyles from '@material-ui/core/styles/makeStyles';
import DoneIcon from "@material-ui/icons/Done";
import BlockIcon from "@material-ui/icons/Block";
import DeleteIcon from '@material-ui/icons/Delete';
import CreateIcon from '@material-ui/icons/Create';
import HelpIcon from '@material-ui/icons/Help';
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import SaveIcon from "@material-ui/icons/Save";
import CancelIcon from "@material-ui/icons/Cancel";
import { useSnackbar } from 'notistack';
import TextField from '@material-ui/core/TextField/TextField';
import {QueueType} from "./Queue/QueueType";


const useStyles = makeStyles((theme: Theme) =>
    createStyles({
        fab: {
            position: 'absolute',
            bottom: theme.spacing(2),
            right: theme.spacing(2),
        },
    })
);

const QueuesPage: React.FC = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [queues, setQueues] = useState<any[] | null>();
    const [editingCellRowId, setEditingCellRowId] = useState<number | null>(null);
    const classes = useStyles();

    const handleCreate = (value: String, newQueue: QueueType) => {
        console.info(newQueue)
        if(queues && value && newQueue){
            APIService.post("/q", newQueue)
            .then(() => {
                enqueueSnackbar("Queue successfully created queue: " + newQueue.name + "" + " - party time!", {
                    variant: 'success',
                });

            })
        }
    }

    const fetchQueues = () => {
        APIService.get("/q")
            .then((response) => { setQueues(response) })
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar("An error occured, please contact support after-party@enioka.com for help.", {
                    variant: 'error',
                })
            })}

    // edit action
    const handleEdit = (value: String, {rowData}: {rowData: any[]} ) => {
        console.info(rowData)
        if(queues && value && rowData){
            const updatedQueueObj = {
                name: rowData[0],
                description: rowData[1],
                defaultQueue: rowData[2],
                id: rowData[3]
            };
            APIService.put("/q/" + value, updatedQueueObj)
            .then(() => {
                const updatedQueues = queues.map(q => q.id === value ? updatedQueueObj : q);
                setQueues(updatedQueues)
                enqueueSnackbar("Queue successfully edited queue: " + rowData ? rowData[0] : "" + " - party time!", {
                    variant: 'success',
                });
            })
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar("An error occured, please contact support after-party@enioka.com for help.", {
                    variant: 'error',
                })
            })
        }
    }

    // delete action
    const handleDelete = async (queueId: String, {rowData}: { rowData: any[]} ) => {
        const queueName = rowData ? rowData[0] : "";
        await deleteQueue(queueId, queueName);
    }

    const deleteQueue = async (queueId: String, queueName: String) => {
        if(queues && queueId) {
            await APIService.delete("/q/" + queueId)
            .then(() => {
                const updatedQueus = queues.filter(q => q.id !== queueId);
                console.log(updatedQueus);
                setQueues(updatedQueus);
                enqueueSnackbar("Queue successfully deleted queue: " + queueName + "- party time!", {
                    variant: 'success',
                });
            })
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar("An error occured, please contact support after-party@enioka.com for help.", {
                    variant: 'error',
                })
            })
        }
    }


    useEffect(() => {
        fetchQueues();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])


    /*
    * Render cell containing boolean value
    */
    const renderBooleanCell = (value: any, tableMeta: any, updateValue: any) => {
        if (editingCellRowId === tableMeta.rowIndex) {
            return <Switch
            //checked={state.checkedA}
            //onChange={handleChange}
            //name="checkedA"
            />
        } else {
            return value ? <DoneIcon /> : <BlockIcon />;
        }
    };

    /**
    * Render cell with action buttons
    */
    const renderActionsCell = (value: any, tableMeta: any, updateValue: any) => {
        if (editingCellRowId === tableMeta.rowIndex) {
            return (<>
                <Tooltip title={"Save changes"}>
                    <IconButton color="default" aria-label={"save"} onClick={() => {
                        setEditingCellRowId(null);
                    }}>
                        <SaveIcon />
                    </IconButton>
                </ Tooltip>
                <Tooltip title={"Cancel changes"}>
                    <IconButton color="default" aria-label={"cancel"} onClick={() => {
                        setEditingCellRowId(null);
                    }}>
                        <CancelIcon />
                    </IconButton>
                </ Tooltip>
            </>)
        } else {
            return (<>
                <Tooltip title={"Edit line"}>
                    <IconButton color="default" aria-label={"edit"} onClick={() => {
                        setEditingCellRowId(tableMeta.rowIndex)
                        //
                    }}>
                        {/* <CreateIcon onclick={() => handleEdit()}/> */}
                        <CreateIcon />
                    </IconButton>
                </ Tooltip>
                <Tooltip title={"Delete line"}>
                    <IconButton
                        color="default"
                        aria-label={"delete"}
                        onClick={
                            () => handleDelete(value, tableMeta)}
                        >
                        <DeleteIcon />
                    </IconButton>
                </ Tooltip>
            </>
            );
        }
    }

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
                            fontSize: "0.8125rem"
                        }
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
                customBodyRender: renderStringCell
            }
        },
        {
            name: "description",
            label: "Description",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderStringCell
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
            name: "id",
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
                            onClick={() => handleCreate("9", {
                                name:"test" + Math.floor(Math.random() * Math.floor(9999999)),
                                description: "description beaaaau",
                                defaultQueue: false
                            })}
                        >
                            <AddCircleIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Refresh"}>
                        <IconButton color="default" aria-label={"refresh"} onClick={() => fetchQueues()}>
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip>
                    <Tooltip title={"Help"}>
                        <IconButton color="default" aria-label={"help"} onClick={() => {
                            //
                        }}>
                            <HelpIcon />
                        </IconButton>
                    </Tooltip>
                </>
            )
        },
        onRowsDelete: ({data} : {data: any[]}) => {
            // get all rows by index
            data.forEach(async ({index}) => {
                console.log(index);
                const currentQueue = queues ? queues[index] : null;
                console.log(currentQueue);
                if(currentQueue){
                    await deleteQueue(currentQueue.id, currentQueue.name);
                }
            });
        },

            //filterType: 'checkbox',
    };

    if (queues) {
        return (<Container>
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
        )
    }
}

export default QueuesPage
