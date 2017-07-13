#!/bin/bash

[ -f $(dirname $0)/../../main/bash/databot.shlib ] && \
    . $(dirname $0)/../../main/bash/databot.shlib || \
    { echo "[error]: $(dirname $0)/../../main/bash/databot.shlib not found" 1>&2; exit 1; }


extract-value-from-yaml-file $@


