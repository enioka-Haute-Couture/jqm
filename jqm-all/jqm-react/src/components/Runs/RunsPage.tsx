import React, { useEffect, useState, useCallback, useRef } from "react";
import { Container, Grid, IconButton, Tooltip } from "@material-ui/core";
import CircularProgress from "@material-ui/core/CircularProgress";
import MUIDataTable from "mui-datatables";
import HelpIcon from "@material-ui/icons/Help";
import RefreshIcon from "@material-ui/icons/Refresh";
import AddCircleIcon from "@material-ui/icons/AddCircle";
import { JobInstance } from "./JobInstance";
import useJobInstanceAPI from "./JobInstanceAPI";


const RunsPage: React.FC = () => {
    const {
        jobInstances,
        fetchJobInstances,
        createJobInstance,
    } = useJobInstanceAPI();

    useEffect(() => {
        fetchJobInstances();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    const columns = [
        {
            name: "id",
            label: "id",
            options: {
                filter: true,
                sort: true,
            }
        },
        {
            name: "applicationName",
            label: "Application",
            options: {
                filter: true,
                sort: true,
            },
        },
        {
            name: "queueName",
            label: "Queue",
            options: {
                filter: true,
                sort: true,
            },
        },
        {
            name: "state",
            label: "Status",
            options: {
                filter: true,
                sort: true,
            },
        },
        {
            name: "",
            label: "Actions",
            options: {
                filter: true,
                sort: true,
                customBodyRender: () => <></>
            },
        },
    ];

    const options = {
        setCellProps: () => ({ fullWidth: "MuiInput-fullWidth" }),
        download: false,
        print: false,
        // serverSide: true,
        // onTableChange: (action: any, tableState: any) => {
        //     this.xhrRequest('my.api.com/tableData', result => {
        //         this.setState({ data: result });
        //     });
        // },
        customToolbar: () => {
            return (
                <>
                    <Tooltip title={"New launch form"}>
                        <>
                            <IconButton
                                color="default"
                                aria-label={"add"}
                            // onClick={() => setShowDialog(true)}
                            >
                                <AddCircleIcon />
                            </IconButton>
                            {/* <CreateQueueDialog
                                showDialog={showDialog}
                                closeDialog={() => setShowDialog(false)}
                                createQueue={createQueue}
                            /> */}
                        </>
                    </Tooltip>
                    {/* <Tooltip title={"Refresh"}>
                        <IconButton
                            color="default"
                            aria-label={"refresh"}
                            onClick={() => fetchQueues()}
                        >
                            <RefreshIcon />
                        </IconButton>
                    </Tooltip> */}
                    <Tooltip title={"Help"}>
                        <IconButton color="default" aria-label={"help"}>
                            <HelpIcon />
                        </IconButton>
                    </Tooltip>
                </>
            );
        },
    };

    return jobInstances ?
        (<Container maxWidth={false}>
            <MUIDataTable
                title={"Runs"}
                data={jobInstances}
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

export default RunsPage;
