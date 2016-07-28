/*
 * Copyright (c) 2016 Nova Ordis LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.novaordis.osstats;

/**

 TODO

 output-file --output=<output-file>  specifies the name of the output file.  If not specified,
 the default file to write to is /tmp/os-stats.csv. /dev/stdout can be used to send the output to
 stdout. Note that if  --foreground   option is used,   the output will forcibly  go to
 /dev/stdout, regardless on whether --output option was used or not.

 --interval=<seconds> specifies the sampling interval length, in seconds.  If not specified the
 default is 10 seconds.

 --foreground -  runs in foreground from the controlling terminal, instead of background, which is
 the default. In foreground mode, the output is switched automatically to /dev/stdout and --output
 option is ignored.

 * @author Ovidiu Feodorov <ovidiu@novaordis.com>
 * @since 7/27/16
 */
public interface Configuration {

    // Constants -------------------------------------------------------------------------------------------------------

    // Static ----------------------------------------------------------------------------------------------------------

    // Public ----------------------------------------------------------------------------------------------------------

}
