/*
 *  Copyright (c) Lightstreamer Srl
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */


package com.lightstreamer.interfaces.metadata;

import java.util.concurrent.CompletionStage;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used by Lightstreamer Kernel to receive requests about sessions and any
 * asynchronous severe error notification from the Metadata Adapter.
 * The listener instance is supplied to the Metadata Adapter by Lightstreamer
 * Kernel through a setListener call.
 * 
 * @see MetadataProvider
 */
public interface MetadataControlListener {

    /**
     * Enforces the termination of the specified session.
     * If currently there is no such session, the call does nothing.
     * The call does nothing also if such session exists but it is bound
     * to a different Adapter Set
     * <BR>If the session is terminated, the originating client will
     * receive the notification of the termination according with the API
     * in use, together with a proper cause code.
     * <BR>Invoking this request while still inside the execution
     * of {@link MetadataProvider#notifyNewSession} related with the
     * supplied session ID is pointless; however, the request will be
     * held, then carried out after the underlying session initiation
     * has finished. If the session termination occurs while the client
     * creation request is still in place, the client request will return
     * as failed.
     * 
     * @param sessionID The ID of the session to be closed.
     * 
     * @return A CompletionStage that provides the operation outcome.
     * Note that any continuations requested to this CompletionStage
     * without explicitly providing an Executor will be performed
     * in the SERVER thread pool, hence they are expected to execute fast.
     * Also note that if the session with the specified ID is not found
     * the operation will be just considered as fulfilled. 
     */
	@Nonnull
    public CompletionStage<Void> forceSessionTermination(@Nonnull String sessionID);

    /**
     * Enforces the termination of the specified session.
     * If currently there is no such session, the call does nothing.
     * The call does nothing also if such session exists but it is bound
     * to a different Adapter Set
     * <BR>If the session is terminated, the originating client will
     * receive the notification of the termination according with the API
     * in use, together with the specified cause code and optional custom message.
     * <BR>Invoking this request while still inside the execution
     * of {@link MetadataProvider#notifyNewSession} related with the
     * supplied session ID is pointless; however, the request will be
     * held, then carried out after the underlying session initiation
     * has finished. If the session termination occurs while the client
     * creation request is still in place, the client request will return
     * as failed.
     * 
     * @param sessionID The ID of the session to be closed.
     * @param causeCode  Error code that can be used to distinguish the
     * cause of the closure. It must be a negative integer, or zero to mean an
     * unspecified problem.
     * @param causeMessage  Error message to be sent to the client, or null.
     * 
     * @return A CompletionStage that provides the operation outcome.
     * Note that any continuations requested to this CompletionStage
     * without explicitly providing an Executor will be performed
     * in the SERVER thread pool, hence they are expected to execute fast.
     * Also note that if the session with the specified ID is not found
     * the operation will be just considered as fulfilled. 
     */
	@Nonnull
    public CompletionStage<Void> forceSessionTermination(@Nonnull String sessionID, int causeCode, @Nullable String causeMessage);

    /**
     * Called by a Metadata Adapter to notify Lightstreamer Kernel of the
     * occurrence of a severe problem that can compromise future operation
     * of the Metadata Adapter.
     * This causes the whole Server to exit, so that an external recovery
     * mechanism may come into action.
     *
     * @param e  any java.lang.Throwable object, with the description of
     * the problem.
     */
    public void failure(@Nonnull Throwable e);
}


/*--- Formatted in Lightstreamer Java Convention Style on 2004-10-29 ---*/
