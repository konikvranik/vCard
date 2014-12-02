#!/usr/bin/env perl

use Text::vCard::Addressbook;
use Data::Dumper;

my $address_book = Text::vCard::Addressbook->load( [ 'addressbook.vcf' ] );
my $dupcnt = 0;
my @krestnijmena;

while ( <krestnijmena.txt> ) {
	push @krestnijmena, $_;
}

sub formatNumber($) {
	my $num = shift;
	$num =~ s/^420/+420/;
	$num =~ s/^00/+/;
	$num =~ s/\s+//g;
	return $num;
}

sub stripName($) {
	my $name = shift;
	$name =~ s/^\W+//;
	$name =~ s/\W+$//;
	return $name;
}

sub stripMail($) {
	my $address = shift;
	if ( $address =~ /<[a-zA-Z0-9_.@-]>/ ) {
		$address =~ s/^.*<[a-zA-Z0-9_.@-]>.*$/\1/;
		return $address;
	}
	$address =~ s/(\S+)\$/\1/;
	return $address;

}

sub merge($$) {
	my $r = shift;
	my $vc = shift;

	if ( uc($r->[0]->{given}) eq uc($vc->[0]->{given}) and uc($r->[0]->{family}) eq uc($vc->[0]->{family})) {

		return 1;
	} else {
		return 0;
	}
}

sub processDupes(@) {
	my @dupes = @_;
	my @references;
	foreach $i ( @dupes ) {
		foreach $reference ( @references ) {
			my $vcard = $i;

			my $given = stripName( $vcard->[0]->{given} );
			my $family = stripName( $vcard->[0]->{family} );
			my $gn = 0;
			my $fm = 0;
			foreach $n ( $krestniJmena ) {
				$gn = 1 if $n eq uc $given;
				$fn = 1 if $n eq uc $family;
			}
			if ( $fn and not $gn ) {
				$vcard->[0]->{given} = $family;
				$vcard->[0]->{family} = $given;
			}

			last if merge( $reference, $vcard );
			push @references, $vcard;
		}
	}
}

sub addToHash($$$) {
	my $hash = shift;
	my $key = shift;
	my $value = shift;
	my %hash = %$hash;

	#printf "KEY: >%s<\n", $key;

	my $kv = $hash{$key};
	if ( $kv ) {
		push @$kv, $value;
		#printf "Found %d dupe candidates.\n", ++$dupcnt;
	} else {
		$hash{$key} = [ $value ];
	}
	return \%hash;
}

my %numbers ;

foreach $i ( $address_book->vcards() ) {
	my $tel = $i->get( { node_type => "TEL" } );
	if ( $tel ) {
		if ( ref $tel eq "ARRAY" ) {
			foreach $j ( @$tel ) {
				my $number = formatNumber( $j->value() );
				%numbers = %{ addToHash( \%numbers, $number, $i ) };
			}
		} else {
			my $number = formatNumber( $tel->value() );
			%numbers = %{ addToHash( \%numbers, $number, $i ) };
		}
	}

	my $mail = $i->get( { node_type => "EMAIL" } );
	if ($mail ) {
		if ( ref $mail eq "ARRAY" ) {
			foreach $j ( @$mail ) {
				my $address = stripMail( $j->value() );
				%numbers = %{ addToHash( \%numbers, $address, $i ) };
			}
		} else {
			my $address = stripMail( $mail->value() );
			%numbers = %{ addToHash( \%numbers, $address, $i ) };
		}
	}

	my $org = $i->get( { node_type => "ORG" } );
	if ($org ) {
		if ( ref $org eq "ARRAY" ) {
			foreach $j ( @$org ) {
				my $name = stripName( $j->{name} );
				%numbers = %{ addToHash( \%numbers, $name, $i ) };
			}
		} else {
			my $name = stripName( $org->{name} );
			%numbers = %{ addToHash( \%numbers, $name, $i ) };
		}
	}

	my $n = $i->get( { node_type => "N" } );
	if ( $n ) {

		#printf "%s\n", Dumper( $n );

		my $given = $n->[0]->{given};
		%numbers = %{ addToHash( \%numbers, stripName( $given ), $i ) };

		my $family = $n->[0]->{family};
		%numbers = %{ addToHash( \%numbers, stripName( $family ), $i ) };

		my $middle = $n->[0]->{middle};
		%numbers = %{ addToHash( \%numbers, stripName( $middle ), $i ) };
	}

}

foreach $k ( keys %numbers ) {
	my $cnt = $#{$numbers{$k}};
	processDupes(@{$numbers{$k}});
	printf "%d: %s\n", $cnt, $k if ( $cnt > 1 );
}

#printf "%s\n", Dumper( \%numbers );
