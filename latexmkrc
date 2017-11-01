$pdf_mode  = 1;
$bibtex_use = 2;
$pdflatex  = 'pdflatex -halt-on-error -file-line-error -shell-escape -interaction=nonstopmode -synctex=1 %O %S';
$clean_ext = "synctex.gz synctex.gz(busy) run.xml xmpi vrb bbl ist glg glo gls ist lol log 1 dpth auxlock %R-figure*.* %R-blx.bib snm nav dvi xmpi tdo";


add_cus_dep('glo', 'gls', 0, 'makeglossaries');
add_cus_dep('acn', 'acr', 0, 'makeglossaries');
add_cus_dep('mp', '1', 0, 'mpost');

sub makeglossaries {
   return system("makeglossaries \"$_[0]\"");
}

sub mpost {
    my ($name, $path) = fileparse( $_[0] );
    my $return = system("mpost \"$_[0]\"");
    if ( ($path ne '') && ($path ne '.\\') && ($path ne './') ) {
        foreach ( "$name.1", "$name.log" ) { move $_, $path; }
    }
    return $return;
}
