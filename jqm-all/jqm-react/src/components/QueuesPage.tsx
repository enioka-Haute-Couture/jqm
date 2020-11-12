import React, { useEffect, useState } from 'react';
import { Container, Grid, IconButton, Tooltip } from '@material-ui/core';
import CircularProgress from "@material-ui/core/CircularProgress";
import APIService from '../utils/APIService';
import MUIDataTable from "mui-datatables";
import makeStyles from '@material-ui/core/styles/makeStyles';
import DoneIcon from "@material-ui/icons/Done";
import BlockIcon from "@material-ui/icons/Block";
import DeleteIcon from '@material-ui/icons/Delete';
import CreateIcon from '@material-ui/icons/Create';
import RefreshIcon from '@material-ui/icons/Refresh';
import HelpIcon from '@material-ui/icons/Help';
import AddCircleIcon from '@material-ui/icons/AddCircle';
import { useSnackbar } from 'notistack';
import {QueueType} from "../types/QueueType";
import { Queue } from '@material-ui/icons';


const useStyles = makeStyles({
});


/**
 * Render cell containing boolean value
 */
const renderBooleanCell = (value: any, tableMeta: any, updateValue: any) => {
    return value ? <DoneIcon /> : <BlockIcon />;
};

/**
* Render cell with action buttons
 */
const renderActionsCell = (onEdit: any, onDelete: any) => {
    return (value: any, tableMeta: any, updateValue: any) => {
        return (<>
            <Tooltip title={"Edit line"}>
                <IconButton color="default" aria-label={"edit"} onClick={() => onEdit(value, tableMeta)}>
                    <CreateIcon />
                </IconButton>
            </ Tooltip>
            <Tooltip title={"Delete line"}>
                <IconButton color="default" aria-label={"delete"} onClick={() => onDelete(value, tableMeta)}>
                    <DeleteIcon />
                </IconButton>
            </ Tooltip>
        </>
    )}
}

const QueuesPage: React.FC = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [queues, setQueues] = useState<any[] | null>();
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
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar("An error occured, please contact support after-party@enioka.com for help.", {
                    variant: 'error',
                })
            })
        }
    }

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
            APIService.delete("/q/" + queueId)
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
        APIService.get("/q")
        .then((response) => { setQueues(response) })
        .catch((reason) => {
            console.log(reason);
            enqueueSnackbar("An error occured, please contact support for help.", {
                variant: 'error',
                persist: true})});
    }, [])

    const columns = [
        {
            name: "name",
            label: "Name",
            options: {
                filter: true,
                sort: true,
            }
        },
        {
            name: "description",
            label: "Description",
            options: {
                filter: true,
                sort: true,
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
                customBodyRender: renderActionsCell(handleEdit, handleDelete),
            }
        }
    ]

    // TODO: header buttons
    // TODO: editable fields

    const options = {
        download: false,
        print: false,
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
                        <IconButton color="default" aria-label={"refresh"} onClick={() => {
                            // fetchQueues();
                        }}>
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
        </Container>);
    } else {
        return (
            <Grid container justify="center">
                <CircularProgress />
            </Grid>
        )
    }
}

export default QueuesPage
