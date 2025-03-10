#!/bin/bash

POOL_NAME="ceph-objectstore.rgw.buckets.data"
cd /tmp
rgw-orphan-list "${POOL_NAME}"
INPUT_FILE="./orphan-list-20250310124953.out"

while IFS= read -r object; do
if rados -p "$POOL_NAME" rm "$object"; then
echo "Successfully deleted: $object"
else
echo "Failed to delete: $object" >&2
fi
done < "$INPUT_FILE"

echo "Deletion process completed"
