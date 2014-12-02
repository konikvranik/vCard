#!/usr/bin/perl -w
use WWW::Google::Contacts;
use Data::Dumper;
use Text::vCard;
use Text::vCard::Addressbook;
use Data::Dumper;
use MIME::Base64;


my $gcontacts = WWW::Google::Contacts->new();
$gcontacts->login('petr.vranik@gmail.com', 'SG13eh72') or die 'login failed';


sub getPhoto($) {
	my $i = shift;
	print STDERR "Getting photo...\n";
	if ( $i->{rel} eq 'http://schemas.google.com/contacts/2008/rel#photo' ) {
		my $type = $i->{type};
		my $url = $i->{href};
			#LWP::UserAgent->new;
		my $resp = $gcontacts->{ua}->get( $url, $gcontacts->{authsub}->auth_params );
		if ( $resp and $resp->is_success and $resp->content ) {
			return $resp->content;
		}
	}
}

sub g2v($) {
	my $v = shift;
	return unless $v;
	my ( $a, $b ) = split /#/, $v, 2;
	$b = "CELL" if lc $b eq "mobile";
	return $b;
}

sub addAddrField($$) {
	$vCard = shift;
	$value = shift;

	if ( $value->{"gd:neighborhood"} or $value->{"gd:street"} or $value->{"gd:city"}  or $value->{"gd:region"} or $value->{"gd:postcode"} or $value->{"gd:country"} ) {
		my $ADR = $vCard->add_node( { node_type => "ADR" } );
		$ADR->extended( $value->{"gd:neighborhood"} );
		$ADR->street( $value->{"gd:street"} );
		$ADR->city( $value->{"gd:city"} );
		$ADR->region( $value->{"gd:region"} );
		$ADR->post_code( $value->{"gd:postcode"} );
		$ADR->country( $value->{"gd:country"} );
		$ADR->add_types( g2v( $value->{"rel"} ) );
	}
	if ( $value->{"formattedAddress"} ) {
		my $LABEL = $vCard->add_node( { node_type => "LABELS" } );
		$LABEL->value($value->{"formattedAddress"});
		$LABEL->add_types( g2v( $value->{"rel"} )  );
	}
}

sub addField($$$@) {
	$vCard = shift;
	$value = shift;
	$type = shift;
	$field = shift;
	$field = "content" unless $field;

	return unless $value;
	return unless $value->{$field};

	my $node = $vCard->add_node( { node_type => $type } );
	#print STDERR "$type / $field\n";
	if ( $type eq "ORG") {
		$node->name($value->{$field});
		$node->add_types( g2v( $value->{"rel"} ) );
	} elsif ( $type eq "PHOTO") {
		$node->value( encode_base64( getPhoto( $value ) ) );
		$node->add_types( sprintf "%s;ENCODING=BASE64", "JPEG" );
	} elsif ( $type eq "TEL") {
		my $v = $value->{$field};
		$v =~ s/\s+//g if $v =~ /^\+?(\d*\s*)*$/;
		$v = "+$v" if $v =~ /^420\d{6,20}$/;
		$node->value( $v );
		$node->add_types( g2v( $value->{"rel"} ) );
	} else {
		$node->value($value->{$field});
		$node->add_types( g2v( $value->{"rel"} ) );
	}
}

sub parseField($$$@) {
	$vCard = shift;
	$value = shift;
	$type = shift;
	$field = shift;

	if ( ( ref $value ) eq "ARRAY" ) {
		foreach $i ( @$value ) {
			addField($vCard, $i, $type, $field )
		}
	} else {
		addField($vCard, $value, $type, $field )
	}

}

sub export($@) {

	my $address_book = shift ;

	my @contacts = @_;
	my $cnt = 0;
	foreach my $contact (@contacts) {

		print STDERR "Parsing contact $cnt...\n";
		$cnt++;

		my $vCard = $address_book->add_vcard();

		my $VERSION = $vCard->add_node( { node_type => "VERSION" } );
		$VERSION->value("3.0");

		my $N = $vCard->add_node( { node_type => "N" } );
		$N->family($contact->{"gd:name"}->{"gd:familyName"} );
		$N->given($contact->{"gd:name"}->{"gd:givenName"} );
		$N->middle($contact->{"gd:name"}->{"gd:additionalName"} );
		$N->prefixes($contact->{"gd:name"}->{"gd:namePrefix"} );
		$N->suffixes($contact->{"gd:name"}->{"gd:nameSuffix"} );
		
		my $FN = $vCard->add_node( { node_type => "FN" } );
		$FN->value($contact->{"gd:name"}->{"gd:fullName"});

		my $address = $contact->{"gd:structuredPostalAddress"};
		if ( ( ref $address ) eq "ARRAY" ) {
			foreach $i ( @$address ) {
				addAddrField($vCard, $i );
			}
		} else {
			addAddrField($vCard, $i );
		}

		parseField($vCard, $contact->{"link"}, "PHOTO", "href" );
		parseField($vCard, $contact->{"gContact:birthday"}, "BDAY", "when" );
		parseField($vCard, $contact->{"gd:phoneNumber"}, "TEL" );
		parseField($vCard, $contact->{"email"}, "EMAIL", "address" );
		parseField($vCard, $contact->{"gd:organization"}, "TITLE", "gd:orgTitle" );
		parseField($vCard, $contact->{"gd:organization"}, "ORG", "gd:orgName" );
		parseField($vCard, $contact, "NOTE", "content" );
		parseField($vCard, $contact->{"gContact:website"}, "URL", "href" );
	}
	return $address_book->export();
}


print STDERR "Getting contacts...\n";
my @contacts = $gcontacts->get_contacts ( max_results => 5000 );

print STDERR "Getting groups...\n";
my @groups = $gcontacts->get_groups ( max_results => 5000 );

my $address_book = Text::vCard::Addressbook->new( { souce_file => 'addressbook.vcf' } );

print export( $address_book, @contacts);

print STDERR Dumper( \@groups );
