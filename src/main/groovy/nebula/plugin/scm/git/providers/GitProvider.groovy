/*
 * Copyright 2014-2019 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nebula.plugin.scm.git.providers

import nebula.plugin.scm.providers.ScmProvider
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.operation.ResetOp
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class GitProvider extends ScmProvider {
    private static Logger logger = Logging.getLogger(GitProvider)
    Grgit repo

    GitProvider(String rootDirectory) {
        repo = Grgit.open([dir: rootDirectory] as Map<String, Object>)
    }

    @Override
    Boolean switchToBranch(String branch) {
        grgitCommand("Switched to branch: ${branch}") {
            repo.checkout([branch: branch, createBranch: true] as Map<String, Object>)
        }
    }

    @Override
    Boolean updateFromRepository() {
        grgitCommand('Fetched and merged current branch') {
            repo.pull()
        }
    }

    @Override
    Boolean commit(String message, List<String> files) {
        grgitCommand("Committed ${files}") {
            repo.add([patterns: files] as Map<String, Object>)
            repo.commit([message: message] as Map<String, Object>)
            repo.push()
        }
    }

    @Override
    Boolean tag(String tagname, String message = null) {
        grgitCommand("Created tag: ${tagname}") {
            repo.tag.add([name: tagname, message: message ?: "Creating ${tagname}", annotate: true] as Map<String, Object>)
            repo.push([tags: true] as Map<String, Object>)
        }
    }

    @Override
    Boolean preChanges(List<String> files) {
        true
    }

    @Override
    Boolean undoChanges() {
        grgitCommand('Reset(hard) to head of current branch') {
            repo.reset([commit: 'HEAD', mode: ResetOp.Mode.HARD] as Map<String, Object>)
        }
    }

    private Boolean grgitCommand(String successMessage, Closure command) {
        try {
            command()
            logger.info(successMessage)
        } catch(ex) {
            logger.info(ex.message)
            logger.debug(ex.message)
            false
        }
        true    
    }
}
