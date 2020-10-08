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
import { useSnackbar } from 'notistack';



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
const renderActionsCell = (value: any, tableMeta: any, updateValue: any) => {
    return (<>
        <Tooltip title={"Edit line"}>
            <IconButton color="default" aria-label={"edit"} onClick={() => {
                //
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

const QueuesPage: React.FC = () => {
    const { enqueueSnackbar } = useSnackbar();
    const [queues, setQueues] = useState<any[] | null>();
    const classes = useStyles();

    useEffect(() => {
        APIService.get("/q")
            .then((response) => { setQueues(response) })
            .catch((reason) => {
                console.log(reason);
                enqueueSnackbar("An error occured, please contact support for help.", {
                    variant: 'error',
                    persist: true
                })
            });
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
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: renderActionsCell,
            }
        }
    ]

    // TODO: header buttons
    // TODO: editable fields

    const options = {
        download: false,
        print: false,
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
