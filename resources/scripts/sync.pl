#!/usr/bin/perl

# Script to synchronize
# Author: Kleyson Rios

use DBI;
use strict;
use MIME::Base64;
use JSON qw( decode_json );
use Getopt::Long qw(GetOptions);

my $user;
my $pentaho_server;
my $pentaho_port;
my $solution;
my $path;

options();

######################################
#
# Dados para a conexao com o Banco de Dados
#
######################################
my $driver   = "Pg";
my $host     = "localhost";
my $port     = 5432;
my $database = "postgres";
my $dsn      = "DBI:$driver:dbname=$database;host=$host;port=$port";
my $userid   = "postgres";
my $password = "postgres";
my $dbh      = DBI->connect($dsn, $userid, $password, { RaiseError => 1 }) or die $DBI::errstr;

print "\nDatabase opened successfully\n";

######################################
#
# New token based on epoch time
#
######################################
my $token = time;

######################################
#
# Encode url
#
######################################
my $urlEncoded = encode_base64("/pentaho/plugin/fastsync/api/sync?ts=" . $token);
$urlEncoded =~ s/[\r\n]+$//;

######################################
#
# Insert token
#
######################################
my $stmt = qq( INSERT INTO portal.tokensbi(toke_username, toke_token, toke_url, toke_usua_codg) VALUES ('$user', '$token', '$urlEncoded', 'KLEYSONRIOS'); );
my $rv = $dbh->do($stmt) or die $DBI::errstr;

print "Token inserted successfully\n";

######################################
#
# Call /sync API
#
######################################
my $api = "http://$pentaho_server:$pentaho_port/pentaho/plugin/fastsync/api/sync --data-urlencode 'type=token' --data-urlencode 'token=$token' --data-urlencode 'solution=$solution' --data-urlencode 'delete=true' --data-urlencode 'deletePerm=true' --data-urlencode 'path=$path' --data-urlencode 'urlEncoded=$urlEncoded'";

my $apiReturn = `/usr/bin/curl -s -G $api`;

my $decodedReturn = decode_json($apiReturn);

print $decodedReturn->{'error_message'} . "\n";
print $decodedReturn->{'message'} . "\n";


sub options() {

    GetOptions('user=s' => \$user,
               'host=s' => \$pentaho_server,
               'port=s' => \$pentaho_port,
               'solution=s' => \$solution,
               'path=s' => \$path) or die "Usage: $0 --user PENTAHO_USER --host PENTAHO_SERVER --port PORT --solution SOLUTION --path PATH\n";

    if (! $solution || ! $path || ! $user || ! $pentaho_server || ! $pentaho_port) {
         die "Usage: $0 --user PENTAHO_USER --host PENTAHO_SERVER --port PORT --solution SOLUTION --path PATH\n";
    }

}