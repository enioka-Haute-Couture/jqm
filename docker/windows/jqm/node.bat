@echo OFF
IF %JQM_CREATE_NODE_IF_MISSING% EQU 1 (
    IF NOT EXIST C:\jqm\db\%JQM_NODE_NAME% (    
        java -jar jqm.jar -u
        java -jar jqm.jar -createnode %JQM_NODE_NAME%
        java -jar jqm.jar -importjobdef ./jobs/

        echo 1 > C:\jqm\db\%JQM_NODE_NAME%
    )
)
