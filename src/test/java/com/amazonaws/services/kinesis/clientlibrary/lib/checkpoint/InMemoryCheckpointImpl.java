/*
 * Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.kinesis.clientlibrary.lib.checkpoint;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.kinesis.clientlibrary.exceptions.KinesisClientLibException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.ICheckpoint;
import com.amazonaws.services.kinesis.clientlibrary.types.ExtendedSequenceNumber;

/**
 * Everything is stored in memory and there is no fault-tolerance.
 */
public class InMemoryCheckpointImpl implements ICheckpoint {

    private static final Log LOG = LogFactory.getLog(InMemoryCheckpointImpl.class);

    private Map<String, ExtendedSequenceNumber> checkpoints = new HashMap<>();
    private Map<String, ExtendedSequenceNumber> flushpoints = new HashMap<>();
    private final String startingSequenceNumber;

    /**
     * Constructor.
     *
     * @param startingSequenceNumber Initial checkpoint will be set to this sequenceNumber (for all shards).
     */
    public InMemoryCheckpointImpl(String startingSequenceNumber) {
        super();
        this.startingSequenceNumber = startingSequenceNumber;
    }

    ExtendedSequenceNumber getLastCheckpoint(String shardId) {
        ExtendedSequenceNumber checkpoint = checkpoints.get(shardId);
        if (checkpoint == null) {
            checkpoint = new ExtendedSequenceNumber(startingSequenceNumber);
        }
        LOG.debug("getLastCheckpoint shardId: " + shardId + " checkpoint: " + checkpoint);
        return checkpoint;
    }

    ExtendedSequenceNumber getLastFlushpoint(String shardId) {
        ExtendedSequenceNumber flushpoint = flushpoints.get(shardId);
        LOG.debug("getLastFlushpoint shardId: " + shardId + " flushpoint: " + flushpoint);
        return flushpoint;
    }

    void resetCheckpointToLastFlushpoint(String shardId) throws KinesisClientLibException {
        ExtendedSequenceNumber currentFlushpoint = flushpoints.get(shardId);
        if (currentFlushpoint == null) {
            checkpoints.put(shardId, new ExtendedSequenceNumber(startingSequenceNumber));
        } else {
            checkpoints.put(shardId, currentFlushpoint);
        }
    }

    ExtendedSequenceNumber getGreatestPrimaryFlushpoint(String shardId) throws KinesisClientLibException {
        verifyNotEmpty(shardId, "shardId must not be null.");
        ExtendedSequenceNumber greatestFlushpoint = getLastFlushpoint(shardId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getGreatestPrimaryFlushpoint value for shardId " + shardId + " = " + greatestFlushpoint);
        }
        return greatestFlushpoint;
    };

    ExtendedSequenceNumber getRestartPoint(String shardId) {
        verifyNotEmpty(shardId, "shardId must not be null.");
        ExtendedSequenceNumber restartPoint = getLastCheckpoint(shardId);
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRestartPoint value for shardId " + shardId + " = " + restartPoint);
        }
        return restartPoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCheckpoint(String shardId, ExtendedSequenceNumber checkpointValue, String concurrencyToken)
        throws KinesisClientLibException {
        checkpoints.put(shardId, checkpointValue);
        flushpoints.put(shardId, checkpointValue);

        if (LOG.isDebugEnabled()) {
            LOG.debug("shardId: " + shardId + " checkpoint: " + checkpointValue);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtendedSequenceNumber getCheckpoint(String shardId) throws KinesisClientLibException {
        ExtendedSequenceNumber checkpoint = flushpoints.get(shardId);
        LOG.debug("getCheckpoint shardId: " + shardId + " checkpoint: " + checkpoint);
        return checkpoint;
    }

    /** Check that string is neither null nor empty.
     */
    static void verifyNotEmpty(String string, String message) {
        if ((string == null) || (string.isEmpty())) {
            throw new IllegalArgumentException(message);
        }
    }

}
