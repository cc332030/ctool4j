#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Apache Maven Wrapper startup batch script, version 3.3.4
#
# Optional ENV vars
# -----------------
#   JAVA_HOME - location of a JDK home dir, required when download maven via java source
#   MVNW_REPOURL - repo url base for downloading maven distribution
#   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
#   MVNW_VERBOSE - true: enable verbose log; debug: trace the mvnw script; others: silence the output
# ----------------------------------------------------------------------------

set -euf
[ "${MVNW_VERBOSE-}" != debug ] || set -x

# OS specific support.
native_path() { printf %s\\n "$1"; }
case "$(uname)" in
CYGWIN* | MINGW*)
  [ -z "${JAVA_HOME-}" ] || JAVA_HOME="$(cygpath --unix "$JAVA_HOME")"
  native_path() { cygpath --path --windows "$1"; }
  ;;
esac

# set JAVACMD and JAVACCMD
set_java_home() {
  # For Cygwin and MinGW, ensure paths are in Unix format before anything is touched
  if [ -n "${JAVA_HOME-}" ]; then
    if [ -x "$JAVA_HOME/jre/sh/java" ]; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
      JAVACCMD="$JAVA_HOME/jre/sh/javac"
    else
      JAVACMD="$JAVA_HOME/bin/java"
      JAVACCMD="$JAVA_HOME/bin/javac"

      if [ ! -x "$JAVACMD" ] || [ ! -x "$JAVACCMD" ]; then
        echo "The JAVA_HOME environment variable is not defined correctly, so mvnw cannot run." >&2
        echo "JAVA_HOME is set to \"$JAVA_HOME\", but \"\$JAVA_HOME/bin/java\" or \"\$JAVA_HOME/bin/javac\" does not exist." >&2
        return 1
      fi
    fi
  else
    JAVACMD="$(
      'set' +e
      'unset' -f command 2>/dev/null
      'command' -v java
    )" || :
    JAVACCMD="$(
      'set' +e
      'unset' -f command 2>/dev/null
      'command' -v javac
    )" || :

    if [ ! -x "${JAVACMD-}" ] || [ ! -x "${JAVACCMD-}" ]; then
      echo "The java/javac command does not exist in PATH nor is JAVA_HOME set, so mvnw cannot run." >&2
      return 1
    fi
  fi
}

# hash string like Java String::hashCode
hash_string() {
  str="${1:-}" h=0
  while [ -n "$str" ]; do
    char="${str%"${str#?}"}"
    h=$(((h * 31 + $(LC_CTYPE=C printf %d "'$char")) % 4294967296))
    str="${str#?}"
  done
  printf %x\\n $h
}

verbose() { :; }
[ "${MVNW_VERBOSE-}" != true ] || verbose() { printf %s\\n "${1-}"; }

die() {
  printf %s\\n "$1" >&2
  exit 1
}

trim() {
  # MWRAPPER-139:
  #   Trims trailing and leading whitespace, carriage returns, tabs, and linefeeds.
  #   Needed for removing poorly interpreted newline sequences when running in more
  #   exotic environments such as mingw bash on Windows.
  printf "%s" "${1}" | tr -d '[:space:]'
}

scriptDir="$(dirname "$0")"
scriptName="$(basename "$0")"

# parse distributionUrl and optional distributionSha256Sum, requires .mvn/wrapper/maven-wrapper.properties
while IFS="=" read -r key value; do
  case "${key-}" in
  distributionUrl) distributionUrl=$(trim "${value-}") ;;
  distributionSha256Sum) distributionSha256Sum=$(trim "${value-}") ;;
  esac
done <"$scriptDir/.mvn/wrapper/maven-wrapper.properties"
[ -n "${distributionUrl-}" ] || die "cannot read distributionUrl property in $scriptDir/.mvn/wrapper/maven-wrapper.properties"

case "${distributionUrl##*/}" in
maven-mvnd-*bin.*)
  MVN_CMD=mvnd.sh _MVNW_REPO_PATTERN=/maven/mvnd/
  case "${PROCESSOR_ARCHITECTURE-}${PROCESSOR_ARCHITEW6432-}:$(uname -a)" in
  *AMD64:CYGWIN* | *AMD64:MINGW*) distributionPlatform=windows-amd64 ;;
  :Darwin*x86_64) distributionPlatform=darwin-amd64 ;;
  :Darwin*arm64) distributionPlatform=darwin-aarch64 ;;
  :Linux*x86_64*) distributionPlatform=linux-amd64 ;;
  *)
    echo "Cannot detect native platform for mvnd on $(uname)-$(uname -m), use pure java version" >&2
    distributionPlatform=linux-amd64
    ;;
  esac
  distributionUrl="${distributionUrl%-bin.*}-$distributionPlatform.zip"
  ;;
maven-mvnd-*) MVN_CMD=mvnd.sh _MVNW_REPO_PATTERN=/maven/mvnd/ ;;
*) MVN_CMD="mvn${scriptName#mvnw}" _MVNW_REPO_PATTERN=/org/apache/maven/ ;;
esac

# apply MVNW_REPOURL and calculate MAVEN_HOME
# maven home pattern: ~/.m2/wrapper/dists/{apache-maven-<version>,maven-mvnd-<version>-<platform>}/<hash>
[ -z "${MVNW_REPOURL-}" ] || distributionUrl="$MVNW_REPOURL$_MVNW_REPO_PATTERN${distributionUrl#*"$_MVNW_REPO_PATTERN"}"
distributionUrlName="${distributionUrl##*/}"
distributionUrlNameMain="${distributionUrlName%.*}"
distributionUrlNameMain="${distributionUrlNameMain%-bin}"
# CNB 构建环境下 $HOME / /tmp 所在文件系统可能不支持文件写入（tar 解压报 Function not implemented），
# 而工作区（CNB_BUILD_WORKSPACE）可正常读写。故在此环境下将 Maven 下载/解压目录重定向到工作区。
if [ -n "${CNB_BUILD_WORKSPACE-}" ] && ! command -v mvn >/dev/null 2>&1; then
  verbose "CNB build env detected, redirect Maven home to workspace: ${CNB_BUILD_WORKSPACE}"
  MAVEN_USER_HOME="${MAVEN_USER_HOME:-${CNB_BUILD_WORKSPACE}.m2}"
  CNB_MAVEN_WORKSPACE_DL="${CNB_BUILD_WORKSPACE}.mvnw-tmp"
  mkdir -p "$CNB_MAVEN_WORKSPACE_DL" 2>/dev/null || :
else
  MAVEN_USER_HOME="${MAVEN_USER_HOME:-${HOME}/.m2}"
fi
MAVEN_HOME="${MAVEN_USER_HOME}/wrapper/dists/${distributionUrlNameMain-}/$(hash_string "$distributionUrl")"

exec_maven() {
  unset MVNW_VERBOSE MVNW_USERNAME MVNW_PASSWORD MVNW_REPOURL || :
  # 修复因解压工具（如 java.util.zip）不保留文件权限、或构建机缓存导致 bin 脚本丢失可执行位的情况。
  [ -x "$MAVEN_HOME/bin/$MVN_CMD" ] || chmod +x "$MAVEN_HOME/bin/$MVN_CMD" "$MAVEN_HOME/bin/mvnDebug" 2>/dev/null || :
  exec "$MAVEN_HOME/bin/$MVN_CMD" "$@" || die "cannot exec $MAVEN_HOME/bin/$MVN_CMD"
}

if [ -d "$MAVEN_HOME" ]; then
  verbose "found existing MAVEN_HOME at $MAVEN_HOME"
  exec_maven "$@"
fi

# CNB 构建环境下 tar 解压 Maven 发行包可能失败（Function not implemented），
# 若系统已安装 maven（CNB 构建环境自带 mvn），则直接使用，避免下载解压失败。
if command -v mvn >/dev/null 2>&1; then
  verbose "found system mvn, using it directly"
  exec mvn "$@"
fi

case "${distributionUrl-}" in
*?-bin.zip | *?maven-mvnd-?*-?*.zip) ;;
*) die "distributionUrl is not valid, must match *-bin.zip or maven-mvnd-*.zip, but found '${distributionUrl-}'" ;;
esac

# prepare tmp dir
# 优先使用工作区下的临时目录（CNB 环境下 /tmp 可能不可写），否则回退 mktemp
TMP_DOWNLOAD_DIR=''
if [ -n "${CNB_MAVEN_WORKSPACE_DL-}" ] && [ -d "$CNB_MAVEN_WORKSPACE_DL" ]; then
  CNB_DL_CANDIDATE="$CNB_MAVEN_WORKSPACE_DL/dl-$$"
  if mkdir -p "$CNB_DL_CANDIDATE" 2>/dev/null; then
    TMP_DOWNLOAD_DIR="$CNB_DL_CANDIDATE"
  fi
fi
if [ -z "$TMP_DOWNLOAD_DIR" ] && command -v mktemp >/dev/null 2>&1; then
  TMP_DOWNLOAD_DIR="$(mktemp -d 2>/dev/null)"
fi
if [ -z "$TMP_DOWNLOAD_DIR" ] || [ ! -d "$TMP_DOWNLOAD_DIR" ]; then
  die "cannot create temp dir"
fi
clean() { rm -rf -- "$TMP_DOWNLOAD_DIR"; }
trap clean HUP INT TERM EXIT

mkdir -p -- "${MAVEN_HOME%/*}"

# Download and Install Apache Maven
verbose "Couldn't find MAVEN_HOME, downloading and installing it ..."
verbose "Downloading from: $distributionUrl"
verbose "Downloading to: $TMP_DOWNLOAD_DIR/$distributionUrlName"

# 保持 .zip 发行包，交由下方解压逻辑处理（unzip -> Java -> tar）。
# 说明：CNB 构建环境文件系统不支持 GNU tar 的部分 syscall，tar 解压报 "Function not implemented"。
# 本仓库强制 JDK 8，故优先使用 JDK 自带的 java.util.zip 解压，避免依赖 unzip/tar 命令行。

# verbose opt
__MVNW_QUIET_WGET=--quiet __MVNW_QUIET_CURL=--silent __MVNW_QUIET_UNZIP=-q __MVNW_QUIET_TAR=''
[ "${MVNW_VERBOSE-}" != true ] || __MVNW_QUIET_WGET='' __MVNW_QUIET_CURL='' __MVNW_QUIET_UNZIP='' __MVNW_QUIET_TAR=v

# normalize http auth
case "${MVNW_PASSWORD:+has-password}" in
'') MVNW_USERNAME='' MVNW_PASSWORD='' ;;
has-password) [ -n "${MVNW_USERNAME-}" ] || MVNW_USERNAME='' MVNW_PASSWORD='' ;;
esac

# 下载 Maven 发行包：支持镜像回退 + 重试。
# 背景：repo.maven.apache.org（Cloudflare 前置）对匿名请求常返回 HTTP 429 限流，
# 且本仓库开启了 set -euf，一次失败即整条流水线失败。故按候选源顺序重试，
# 全部失败后才降级到 Java 下载兜底。
# 可用 MVNW_MIRRORS 覆盖候选源（空格分隔，按 /org/apache/maven/ 之后的相对路径拼接）。
# 本地文件路径（file:// 或绝对路径）直接下载，不拼接镜像基址。
mvnw_download_ok=false
mvnw_download_rc=1

mvnw_download_target="$TMP_DOWNLOAD_DIR/$distributionUrlName"

# 统一按 curl -> wget 顺序尝试；两者都不可用时返回 127 交由 Java 兜底
download_url() {
  # 注意：函数内需用具名变量而非 $1，避免嵌套调用时位置参数被覆盖
  mvnw_dl_url="$1"
  if [ -z "${MVNW_USERNAME-}" ] && command -v curl >/dev/null; then
    verbose "Downloading with curl: $mvnw_dl_url"
    curl ${__MVNW_QUIET_CURL:+"$__MVNW_QUIET_CURL"} -f -L --retry 3 --retry-delay 2 \
      -o "$mvnw_download_target" "$mvnw_dl_url"
    return $?
  fi
  if [ -z "${MVNW_USERNAME-}" ] && command -v wget >/dev/null; then
    verbose "Downloading with wget: $mvnw_dl_url"
    wget ${__MVNW_QUIET_WGET:+"$__MVNW_QUIET_WGET"} "$mvnw_dl_url" -O "$mvnw_download_target"
    return $?
  fi
  return 127
}

# 构造候选下载地址列表
if [ -n "${MVNW_MIRRORS-}" ]; then
  mvnw_mirror_list="$MVNW_MIRRORS"
else
  mvnw_mirror_list="https://repo.maven.apache.org/maven2/ https://maven.aliyun.com/repository/public/ https://repo1.maven.org/maven2/"
fi

case "$distributionUrl" in
file://* | /*)
  mvnw_candidate_urls="$distributionUrl"
  ;;
*)
  mvnw_rel_path="$distributionUrl"
  case "$mvnw_rel_path" in
  *"${_MVNW_REPO_PATTERN-}"*) mvnw_rel_path="${mvnw_rel_path#*"$_MVNW_REPO_PATTERN"}" ;;
  esac
  mvnw_candidate_urls=""
  for mvnw_mirror in $mvnw_mirror_list; do
    # 归一：去掉镜像末尾斜杠、补上仓库前缀（_MVNW_REPO_PATTERN 末尾已带 /）
    mvnw_mirror="${mvnw_mirror%/}"
    mvnw_candidate_urls="$mvnw_candidate_urls $mvnw_mirror${_MVNW_REPO_PATTERN-}$mvnw_rel_path"
  done
  # 候选源均未命中原始地址时，追加原地址兜底
  case " $mvnw_candidate_urls " in
  *" $distributionUrl "*) ;;
  *) mvnw_candidate_urls="$mvnw_candidate_urls $distributionUrl" ;;
  esac
  ;;
esac

for mvnw_candidate_url in $mvnw_candidate_urls; do
  [ "$mvnw_download_ok" = true ] && break
  mvnw_attempt=1
  while [ "$mvnw_attempt" -le 3 ]; do
    verbose "Downloading Maven distribution from: $mvnw_candidate_url (attempt $mvnw_attempt)"
    # 关闭 set -e 以捕获真实返回码：if 语句自身的状态恒为 0，不能用作判断依据
    mvnw_download_rc=0
    download_url "$mvnw_candidate_url" || mvnw_download_rc=$?
    if [ "$mvnw_download_rc" = 0 ]; then
      mvnw_download_ok=true
      break
    fi
    if [ "$mvnw_download_rc" = 127 ]; then
      # 无 curl / wget（或配置了认证信息走 Java 下载），后续候选源同样不可用，直接退出双层循环
      mvnw_attempt=99
      break
    fi
    mvnw_attempt=$((mvnw_attempt + 1))
    [ "$mvnw_attempt" -le 3 ] && sleep 2
  done
done

if [ "$mvnw_download_ok" != true ] && [ "$mvnw_download_rc" = 127 ]; then
  mvnw_download_ok=jar
fi

if [ "$mvnw_download_ok" = true ]; then
  verbose "Maven distribution downloaded successfully"
elif [ "$mvnw_download_ok" = jar ]; then
  if set_java_home; then
    verbose "Falling back to use Java to download"
    javaSource="$TMP_DOWNLOAD_DIR/Downloader.java"
    targetZip="$TMP_DOWNLOAD_DIR/$distributionUrlName"
    cat >"$javaSource" <<-END
	public class Downloader extends java.net.Authenticator
	{
	  protected java.net.PasswordAuthentication getPasswordAuthentication()
	  {
	    return new java.net.PasswordAuthentication( System.getenv( "MVNW_USERNAME" ), System.getenv( "MVNW_PASSWORD" ).toCharArray() );
	  }
	  public static void main( String[] args ) throws Exception
	  {
	    setDefault( new Downloader() );
	    java.nio.file.Files.copy( java.net.URI.create( args[0] ).toURL().openStream(), java.nio.file.Paths.get( args[1] ).toAbsolutePath().normalize() );
	  }
	}
	END
    # For Cygwin/MinGW, switch paths to Windows format before running javac and java
    # -encoding UTF-8：内联 Java 源码为 UTF-8（含中文注释），javac 默认取平台编码，
    # 在 POSIX/ASCII locale（如 LANG=C）下会因无法映射多字节字符而编译失败
    verbose " - Compiling Downloader.java ..."
    "$(native_path "$JAVACCMD")" -encoding UTF-8 "$(native_path "$javaSource")" || die "Failed to compile Downloader.java"
    verbose " - Running Downloader.java ..."
    "$(native_path "$JAVACMD")" -cp "$(native_path "$TMP_DOWNLOAD_DIR")" Downloader "$distributionUrl" "$(native_path "$targetZip")"
  fi
fi

# If specified, validate the SHA-256 sum of the Maven distribution zip file
if [ -n "${distributionSha256Sum-}" ]; then
  distributionSha256Result=false
  if [ "$MVN_CMD" = mvnd.sh ]; then
    echo "Checksum validation is not supported for maven-mvnd." >&2
    echo "Please disable validation by removing 'distributionSha256Sum' from your maven-wrapper.properties." >&2
    exit 1
  elif command -v sha256sum >/dev/null; then
    if echo "$distributionSha256Sum  $TMP_DOWNLOAD_DIR/$distributionUrlName" | sha256sum -c - >/dev/null 2>&1; then
      distributionSha256Result=true
    fi
  elif command -v shasum >/dev/null; then
    if echo "$distributionSha256Sum  $TMP_DOWNLOAD_DIR/$distributionUrlName" | shasum -a 256 -c >/dev/null 2>&1; then
      distributionSha256Result=true
    fi
  else
    echo "Checksum validation was requested but neither 'sha256sum' or 'shasum' are available." >&2
    echo "Please install either command, or disable validation by removing 'distributionSha256Sum' from your maven-wrapper.properties." >&2
    exit 1
  fi
  if [ $distributionSha256Result = false ]; then
    echo "Error: Failed to validate Maven distribution SHA-256, your Maven distribution might be compromised." >&2
    echo "If you updated your Maven version, you need to update the specified distributionSha256Sum property." >&2
    exit 1
  fi
fi

# unzip and move
# 解压优先级：unzip -> Java(zip) -> tar。
# CNB 构建环境不支持 GNU tar 的部分 syscall（报 Function not implemented），
# 本仓库强制 JDK 8，因此当 unzip 不可用时改用 JDK 自带 java.util.zip 解压 .zip。
_extracted=false
if command -v unzip >/dev/null; then
  if unzip ${__MVNW_QUIET_UNZIP:+"$__MVNW_QUIET_UNZIP"} "$TMP_DOWNLOAD_DIR/$distributionUrlName" -d "$TMP_DOWNLOAD_DIR"; then
    _extracted=true
  fi
fi

# unzip 不可用或解压失败时，尝试用 JDK 的 java.util.zip 解压 .zip（绕过 GNU tar 的受限 syscall）
if [ "$_extracted" != true ] && [ "${distributionUrlName##*.}" = zip ] && set_java_home; then
  javaExtractor="$TMP_DOWNLOAD_DIR/Extractor.java"
  cat >"$javaExtractor" <<'END'
import java.io.*;
import java.util.zip.*;

/** 用 JDK 自带的 java.util.zip 解压 zip，仅使用基础文件读写，规避部分环境对 tar syscall 的限制。 */
public class Extractor {
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println("usage: Extractor <zip> <targetDir>");
      System.exit(1);
    }
    File target = new File(args[1]);
    try (ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(args[0])))) {
      ZipEntry entry;
      byte[] buf = new byte[8192];
      while ((entry = in.getNextEntry()) != null) {
        String name = entry.getName();
        File outFile = new File(target, name);
        String canonical = outFile.getCanonicalPath();
        if (!canonical.startsWith(target.getCanonicalPath() + File.separator) && !canonical.equals(target.getCanonicalPath())) {
          throw new IOException("path traversal blocked: " + name);
        }
        if (entry.isDirectory()) {
          outFile.mkdirs();
        } else {
          File parent = outFile.getParentFile();
          if (parent != null) {
            parent.mkdirs();
          }
          try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile))) {
            int n;
            while ((n = in.read(buf)) != -1) {
              out.write(buf, 0, n);
            }
          }
        }
        in.closeEntry();
      }
    }
  }
}
END
  # -encoding UTF-8：Extractor.java 含中文注释，须显式指定源码编码（javac 默认取平台编码，
  # POSIX/ASCII locale 下会报 unmappable character for encoding ASCII 而编译失败）
  verbose "Compiling Extractor.java with javac ..."
  if "$(native_path "$JAVACCMD")" -encoding UTF-8 "$(native_path "$javaExtractor")" \
      && "$(native_path "$JAVACMD")" -cp "$(native_path "$TMP_DOWNLOAD_DIR")" Extractor \
         "$(native_path "$TMP_DOWNLOAD_DIR/$distributionUrlName")" "$(native_path "$TMP_DOWNLOAD_DIR")"; then
    _extracted=true
  fi
fi

# last resort: tar extract .tar.gz (zip path won't reach here since we keep .zip)
if [ "$_extracted" != true ]; then
  tar xzf${__MVNW_QUIET_TAR:+"$__MVNW_QUIET_TAR"} "$TMP_DOWNLOAD_DIR/$distributionUrlName" -C "$TMP_DOWNLOAD_DIR" || die "failed to untar"
fi

if [ "$_extracted" != true ]; then
  die "failed to extract Maven distribution: $distributionUrlName"
fi

# Find the actual extracted directory name (handles snapshots where filename != directory name)
actualDistributionDir=""

# First try the expected directory name (for regular distributions)
if [ -d "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain" ]; then
  if [ -f "$TMP_DOWNLOAD_DIR/$distributionUrlNameMain/bin/$MVN_CMD" ]; then
    actualDistributionDir="$distributionUrlNameMain"
  fi
fi

# If not found, search for any directory with the Maven executable (for snapshots)
if [ -z "$actualDistributionDir" ]; then
  # enable globbing to iterate over items
  set +f
  for dir in "$TMP_DOWNLOAD_DIR"/*; do
    if [ -d "$dir" ]; then
      if [ -f "$dir/bin/$MVN_CMD" ]; then
        actualDistributionDir="$(basename "$dir")"
        break
      fi
    fi
  done
  set -f
fi

if [ -z "$actualDistributionDir" ]; then
  verbose "Contents of $TMP_DOWNLOAD_DIR:"
  verbose "$(ls -la "$TMP_DOWNLOAD_DIR")"
  die "Could not find Maven distribution directory in extracted archive"
fi

verbose "Found extracted Maven distribution directory: $actualDistributionDir"
# 解压工具可能不保留可执行位（java.util.zip 解压的脚本权限默认为 644），此处统一补上。
chmod +x "$TMP_DOWNLOAD_DIR/$actualDistributionDir/bin/$MVN_CMD" "$TMP_DOWNLOAD_DIR/$actualDistributionDir/bin/mvnDebug" 2>/dev/null || :
printf %s\\n "$distributionUrl" >"$TMP_DOWNLOAD_DIR/$actualDistributionDir/mvnw.url"
mv -- "$TMP_DOWNLOAD_DIR/$actualDistributionDir" "$MAVEN_HOME" || [ -d "$MAVEN_HOME" ] || die "fail to move MAVEN_HOME"

clean || :
exec_maven "$@"
